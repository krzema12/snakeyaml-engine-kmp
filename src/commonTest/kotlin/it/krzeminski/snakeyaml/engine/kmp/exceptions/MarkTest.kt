package it.krzeminski.snakeyaml.engine.kmp.exceptions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MarkTest : FunSpec({
    test("Mark snippet") {
        var mark = createMark(0, 0, 0, "*The first line.\nThe last line.", 0)
        mark.createSnippet() shouldBe "    *The first line.\n    ^"
        mark = createMark(0, 0, 0, "The first*line.\nThe last line.", 9)
        mark.createSnippet() shouldBe "    The first*line.\n             ^"
    }

    test("Mark toString()") {
        val mark = createMark(0, 0, 0, "*The first line.\nThe last line.", 0)
        val lines = mark.toString().split("\n")
        lines[0] shouldBe " in test1, line 1, column 1:"
        lines[1].trim() shouldBe "*The first line."
        lines[2].trim() shouldBe "^"
    }

    test("Mark position") {
        val mark = createMark(17, 29, 213, "*The first line.\nThe last line.", 0)
        mark.index shouldBe 17 // index is used in JRuby
        mark.line shouldBe 29
        mark.column shouldBe 213
    }

    test("Mark buffer") {
        val mark = createMark(0, 29, 213, "*The first line.\nThe last line.", 0)
        val buffer = listOf(42, 84, 104, 101, 32, 102, 105, 114, 115, 116, 32, 108, 105, 110, 101,
            46, 10, 84, 104, 101, 32, 108, 97, 115, 116, 32, 108, 105, 110, 101, 46)
        mark.codepoints.size shouldBe buffer.size
        mark.codepoints shouldBe buffer
    }

    test("Mark pointer") {
        val mark = createMark(0, 29, 213, "*The first line.\nThe last line.", 5)
        mark.pointer shouldBe 5
        mark.name shouldBe "test1"
    }

    test("Mark: createSnippet(): longer content must be reduced") {
        val mark = createMark(
            200,
            2,
            36,
            "*The first line,\nThe second line.\nThe third line, which aaaa bbbb ccccc dddddd * contains mor12345678901234\nThe last line.",
            78
        )
        mark.createSnippet(2, 55) shouldBe "   ... aaaa bbbb ccccc dddddd * contains mor1234567 ... \n                             ^"
    }
})

private fun createMark(
    index: Int,
    line: Int,
    column: Int,
    str: String,
    pointer: Int
): Mark {
    val codepoints = str.codePointSequence().toList()
    return Mark("test1", index, line, column, codepoints, pointer)
}
