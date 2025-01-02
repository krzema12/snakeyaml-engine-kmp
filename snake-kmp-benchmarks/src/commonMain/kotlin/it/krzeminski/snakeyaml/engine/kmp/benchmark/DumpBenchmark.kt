package it.krzeminski.snakeyaml.engine.kmp.benchmark

import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import kotlinx.benchmark.*
import okio.Buffer

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
class DumpBenchmark {
    private val dumper = Dump(
        DumpSettings.builder()
            .build(),
    )

    private val streamDataWriter = object : StreamDataWriter {
        val buffer = Buffer()
        override fun write(str: String) {
            buffer.writeUtf8(str)
        }

        override fun write(str: String, off: Int, len: Int) {
            buffer.writeUtf8(str, off, len)
        }

    }

    private val objectToDump: Any = linkedMapOf(
        "byte" to 1.toByte(),
        "short" to 1.toShort(),
        "int" to 1.toInt(),
        "long" to 1.toLong(),
        "float" to 1.5.toFloat(),
        "double" to 1.5.toDouble(),
        "char" to 'c',
        "string" to "test",
    )

    @Benchmark
    fun dumpMapWithAllTypes(blackhole: Blackhole) {
        dumper.dump(objectToDump, streamDataWriter)

        blackhole.consume(streamDataWriter.buffer)
    }
}
