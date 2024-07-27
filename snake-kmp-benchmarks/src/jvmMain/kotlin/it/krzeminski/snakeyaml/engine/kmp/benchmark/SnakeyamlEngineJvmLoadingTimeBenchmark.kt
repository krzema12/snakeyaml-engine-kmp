package it.krzeminski.snakeyaml.engine.kmp.benchmark

import kotlinx.benchmark.*
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.YamlUnicodeReader
import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.constructor.BaseConstructor
import org.snakeyaml.engine.v2.constructor.StandardConstructor
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class SnakeyamlEngineJvmLoadingTimeBenchmark {
    @Param("")
    var openAiYamlPath: String = ""

    private val loadSettings = LoadSettings.builder().build()

    private lateinit var constructor: BaseConstructor

    @Setup
    fun setUp() {
        constructor = StandardConstructor(loadSettings)
    }

    @Benchmark
    fun loadsOpenAiSchema(): Map<*, *> {
        return with(fileSystem()) {
            openReadOnly(openAiYamlPath.toPath(normalize = true)).use { handle ->
                handle.source().buffer().use { source ->
                    val reader = StreamReader(
                        loadSettings,
                        YamlUnicodeReader(source.inputStream()).readText(),
                    )
                    val composer = Composer(
                        loadSettings,
                        ParserImpl(
                            loadSettings,
                            reader,
                        )
                    )
                    constructor.constructSingleDocument(composer.getSingleNode()) as Map<*, *>
                }
            }
        }
    }
}
