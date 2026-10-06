package it.krzeminski.snakeyaml.engine.kmp.array

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings

class PrimitiveArrayTest : FunSpec({
    val bytes = byteArrayOf(1, 2, 3)
    val shorts = shortArrayOf(300, 301, 302)
    val ints = intArrayOf(40000, 40001, 40002)
    val longs = longArrayOf(5000000000L, 5000000001L)
    val floats = floatArrayOf(0.1f, 3.1415f)
    val doubles = doubleArrayOf(50.0001, 2150.0002)
    val chars = charArrayOf('a', 'b', 'c', 'd', 'e')
    val bools = booleanArrayOf(true, false)

    test("represent array of primitives") {
        val dumper = Dump(DumpSettings())
        dumper.dumpToString(bytes) shouldBe "!!binary |-\n  AQID\n"

        dumper.dumpToString(shorts) shouldBe "[300, 301, 302]\n"
        dumper.dumpToString(ints) shouldBe "[40000, 40001, 40002]\n"
        dumper.dumpToString(longs) shouldBe "[5000000000, 5000000001]\n"
        dumper.dumpToString(floats) shouldBe "[0.1, 3.1415]\n"
        dumper.dumpToString(doubles) shouldBe "[50.0001, 2150.0002]\n"
        dumper.dumpToString(chars) shouldBe "[a, b, c, d, e]\n"
        dumper.dumpToString(bools) shouldBe "[true, false]\n"
    }

    test("represent multi array of bytes") {
        val bytes3 = arrayOf(byteArrayOf(1, 2, 3), byteArrayOf(11, 12, 13))
        Dump(DumpSettings()).dumpToString(bytes3) shouldBe
            "- !!binary |-\n  AQID\n- !!binary |-\n  CwwN\n"
    }

    test("represent multi array of int primitives") {
        val ints3 = arrayOf(intArrayOf(1, 2, 3), intArrayOf(11, 12, 13))
        Dump(DumpSettings()).dumpToString(ints3) shouldBe "- [1, 2, 3]\n- [11, 12, 13]\n"
    }

    test("represent multi array of integers") {
        val ints3 = arrayOf(arrayOf(1, 2, 3), arrayOf(11, 12, 13))
        Dump(DumpSettings()).dumpToString(ints3) shouldBe "- [1, 2, 3]\n- [11, 12, 13]\n"
    }
})
