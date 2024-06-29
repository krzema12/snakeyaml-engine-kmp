package it.krzeminski.snakeyaml.engine.kmp.benchmark

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.YamlUnicodeReader
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import it.krzeminski.snakeyaml.engine.kmp.constructor.StandardConstructor
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import kotlinx.benchmark.*
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
class LoadingTimeBenchmark {
    @Param("")
    var openAiYamlPath: String = ""

    private val loadSettings = LoadSettings.builder().build()

    private lateinit var constructor: BaseConstructor

    @Setup
    fun setUp() {
        constructor = StandardConstructor(loadSettings)
    }

    @Benchmark
    fun loadsOpenAiSchema(): Map<*, *>? {
        return with(FILE_SYSTEM) {
            openReadOnly(openAiYamlPath.toPath(normalize = true)).use { handle ->
                handle.source().buffer().use { source ->
                    // TODO: there is a Load class in JVM sources that can handle all of it
                    //       but it is not available for common code.
                    //       Probably, it should be moved from JVM sources to common sources.
                    val reader = StreamReader(
                        loadSettings = loadSettings,
                        stream = YamlUnicodeReader(source),
                    )
                    val composer = Composer(
                        settings = loadSettings,
                        parser = ParserImpl(
                            settings = loadSettings,
                            reader = reader,
                        )
                    )
                    constructor.constructSingleDocument(composer.getSingleNode()) as? Map<*, *>
                }
            }
        }
    }

    private companion object {
        private val FILE_SYSTEM: FileSystem = fileSystem()
    }
}
