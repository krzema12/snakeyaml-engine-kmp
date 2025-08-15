package it.krzeminski.snakeyaml.engine.kmp.issues.issue67

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class DumpLineBreakTest : FunSpec({
    test("dump default scalar style") {
        val dumpSettings = DumpSettings.builder().build()
        dumpSettings.defaultScalarStyle shouldBe ScalarStyle.PLAIN
    }

    test("dump literal scalar style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.LITERAL)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe "|2+\n\n"
        dump.dumpToString("") shouldBe "\"\"\n"
        dump.dumpToString(" ") shouldBe "\" \"\n"
    }

    test("dump JSON scalar style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.JSON_SCALAR_STYLE)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe "\"\\n\"\n"
        dump.dumpToString("") shouldBe "\"\"\n"
        dump.dumpToString(" ") shouldBe "\" \"\n"
    }

    test("dump plain scalar style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe "|2+\n\n"
        dump.dumpToString("") shouldBe "''\n"
        dump.dumpToString(" ") shouldBe "' '\n"
    }

    test("dump folded scalar style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.FOLDED)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe ">2+\n\n"
        dump.dumpToString("") shouldBe "\"\"\n"
        dump.dumpToString(" ") shouldBe "\" \"\n"
    }

    test("dump single quoted style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe "'\n\n  '\n"
        dump.dumpToString("") shouldBe "''\n"
        dump.dumpToString(" ") shouldBe "' '\n"
    }

    test("dump double quoted style") {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .build()
        val dump = Dump(dumpSettings)
        dump.dumpToString("\n") shouldBe "\"\\n\"\n"
        dump.dumpToString("") shouldBe "\"\"\n"
        dump.dumpToString(" ") shouldBe "\" \"\n"
    }
})
