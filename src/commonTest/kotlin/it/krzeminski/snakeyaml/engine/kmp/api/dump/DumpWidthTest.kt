package it.krzeminski.snakeyaml.engine.kmp.api.dump

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class DumpWidthTest : FunSpec({
    fun dumpSettingWithSplit() = DumpSettings.builder().setSplitLines(true)
    fun dumpSettingWithoutSplit() = DumpSettings.builder().setSplitLines(false)

    val data1 = "1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000"
    val data2 = "1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000"

    test("split lines double quoted") {
        val dump = Dump(dumpSettingWithSplit().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build())
        // Split lines enabled (default)
        var output = dump.dumpToString(data1)
        output shouldBe "\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\\\n  \\ 9999999999 0000000000\"\n"

        // Lines with double spaces can be split too as whitespace can be preserved
        output = dump.dumpToString(data2)
        output shouldBe "\"1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777\\\n  \\  8888888888  9999999999  0000000000\"\n"

        // Split lines disabled
        val dump2 = Dump(dumpSettingWithoutSplit().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build())

        output = dump2.dumpToString(data1)
        output shouldBe "\"1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\"\n"

        // setWidth
        val dump3 = Dump(dumpSettingWithSplit().setWidth(15).setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build())
        output = dump3.dumpToString(data1)
        output shouldBe "\"1111111111 2222222222\\\n" + "  \\ 3333333333 4444444444\\\n" +
                "  \\ 5555555555 6666666666\\\n" + "  \\ 7777777777 8888888888\\\n" +
                "  \\ 9999999999 0000000000\"\n"
    }

    test("split lines single quoted") {
        val dump = Dump(dumpSettingWithSplit().setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED).build())
        // Split lines enabled (default)
        var output = dump.dumpToString(data1)
        output shouldBe "'1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000'\n"

        // Do not split on double space as whitespace cannot be preserved in single quoted style
        output = dump.dumpToString(data2)
        output shouldBe "'1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000'\n"

        // Split lines disabled
        val dump2 = Dump(dumpSettingWithoutSplit().setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED).build())

        output = dump2.dumpToString(data1)
        output shouldBe "'1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000'\n"
    }

    test("split lines folded") {
        val dump = Dump(dumpSettingWithSplit().setDefaultScalarStyle(ScalarStyle.FOLDED).build())
        // Split lines enabled (default)
        var output = dump.dumpToString(data1)
        output shouldBe ">-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n"
        val str = Load().loadOne(
            ">-\n\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n"
        ) as String
        str shouldBe "\n$data1" // "No LF must be added"

        // Do not split on double space as whitespace cannot be preserved in folded style
        output = dump.dumpToString(data2)
        output shouldBe ">-\n  1111111111  2222222222  3333333333  4444444444  5555555555  6666666666  7777777777  8888888888  9999999999  0000000000\n"

        // Split lines disabled
        val dump2 = Dump(dumpSettingWithoutSplit().setDefaultScalarStyle(ScalarStyle.FOLDED).build())

        output = dump2.dumpToString(data1)
        output shouldBe ">-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\n"
    }

    test("split lines literal") {
        val dump = Dump(dumpSettingWithSplit().setDefaultScalarStyle(ScalarStyle.LITERAL).build())
        val output = dump.dumpToString(data1)
        // Split lines enabled (default) -- split lines does not apply to literal style
        output shouldBe "|-\n  1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888 9999999999 0000000000\n"
    }

    test("split lines plain") {
        val dump = Dump(dumpSettingWithSplit().setDefaultScalarStyle(ScalarStyle.PLAIN).build())
        // Split lines enabled (default)
        var output = dump.dumpToString(data1)
        output shouldBe "1111111111 2222222222 3333333333 4444444444 5555555555 6666666666 7777777777 8888888888\n  9999999999 0000000000\n"

        // Do not split on double space as whitespace cannot be preserved in plain style
        output = dump.dumpToString(data2)
        output shouldBe "$data2\n"

        // Split lines disabled
        val dump2 = Dump(dumpSettingWithoutSplit().setDefaultScalarStyle(ScalarStyle.PLAIN).build())

        output = dump2.dumpToString(data1)
        output shouldBe "$data1\n"
    }
})
