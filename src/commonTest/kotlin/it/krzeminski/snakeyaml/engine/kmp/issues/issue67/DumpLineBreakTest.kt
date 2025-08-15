package it.krzeminski.snakeyaml.engine.kmp.issues.issue67

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class DumpLineBreakTest : FunSpec({

    test("dump default scalar style") {
        val dumpSettings = DumpSettings.builder().build()
        dumpSettings.defaultScalarStyle shouldBe ScalarStyle.PLAIN
    }

    test("dump literal scalar style") {
        check(style = ScalarStyle.LITERAL, yaml = "\n", expected = "|2+\n\n");
        check(style = ScalarStyle.LITERAL, yaml = "", expected = "\"\"\n");
        check(style = ScalarStyle.LITERAL, yaml = " ", expected = "\" \"\n");
    }

    test("dump JSON scalar style") {
        check(style = ScalarStyle.JSON_SCALAR_STYLE, yaml = "\n", expected = "\"\\n\"\n");
        check(style = ScalarStyle.JSON_SCALAR_STYLE, yaml = "", expected = "\"\"\n");
        check(style = ScalarStyle.JSON_SCALAR_STYLE, yaml = " ", expected = "\" \"\n");
    }

    test("dump plain scalar style") {
        check(style = ScalarStyle.PLAIN, yaml = "\n", expected = "|2+\n\n");
        check(style = ScalarStyle.PLAIN, yaml = "", expected = "''\n");
        check(style = ScalarStyle.PLAIN, yaml = " ", expected = "' '\n");
    }

    test("dump folded scalar style") {
        check(style = ScalarStyle.FOLDED, yaml = "\n", expected = ">2+\n\n");
        check(style = ScalarStyle.FOLDED, yaml = "", expected = "\"\"\n");
        check(style = ScalarStyle.FOLDED, yaml = " ", expected = "\" \"\n");
    }

    test("dump single quoted style") {
        check(style = ScalarStyle.SINGLE_QUOTED, yaml = "\n", expected = "'\n\n  '\n");
        check(style = ScalarStyle.SINGLE_QUOTED, yaml = "", expected = "''\n");
        check(style = ScalarStyle.SINGLE_QUOTED, yaml = " ", expected = "' '\n");
    }

    test("dump double quoted style") {
        check(style = ScalarStyle.DOUBLE_QUOTED, yaml = "\n", expected = "\"\\n\"\n");
        check(style = ScalarStyle.DOUBLE_QUOTED, yaml = "", expected = "\"\"\n");
        check(style = ScalarStyle.DOUBLE_QUOTED, yaml = " ", expected = "\" \"\n");
    }

    test("use Keep in Literal scalar") {
        val input = "---\n" + "top:\n" + "  foo:\n" + "  - problem: |2+\n" + "\n" + "  bar: baz\n"
        val obj = load.loadOne(input)
        obj.shouldNotBeNull()
    }

    test("use Keep in Literal scalar: S98Z") {
        val input = "empty block scalar: >\n" + " \n" + "  \n" + "   \n" + " # comment"
        val obj = load.loadOne(input)
        obj.shouldNotBeNull()
    }
})

private val loadSettings = LoadSettings.builder().build()
private val load = Load(loadSettings)

private fun check(style: ScalarStyle, yaml: String, expected: String) {
    val dumpSettings = DumpSettings.builder()
        .setDefaultScalarStyle(style)
        .build()
    val dump = Dump(dumpSettings)
    val dumpString = dump.dumpToString(yaml)
    dumpString shouldBe expected
    load.loadOne(dumpString) shouldBe yaml
}
