package it.krzeminski.snakeyaml.engine.kmp.array

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings

/** Keeps upstream's exact float values, which only dump as shortest decimals on the JVM. */
class PrimitiveArrayJvmTest : FunSpec({
    test("represent array of floats with upstream values") {
        Dump(DumpSettings()).dumpToString(floatArrayOf(0.1f, 3.1415f)) shouldBe "[0.1, 3.1415]\n"
    }
})
