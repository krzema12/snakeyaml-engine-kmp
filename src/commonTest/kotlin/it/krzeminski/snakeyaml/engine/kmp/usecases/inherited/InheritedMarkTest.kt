package it.krzeminski.snakeyaml.engine.kmp.usecases.inherited

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.exceptions.Mark
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.toCodePoints

class InheritedMarkTest: FunSpec({
    test("Marks") {
        val content = getResource("test_mark.marks")
        val inputs = content.split("---\n")
            // Necessary because 'split' returns also what's before the first
            // delimiter, as an empty string.
            .dropWhile { it.isEmpty() }
        inputs.forEach { input ->
            var index = 0
            var line = 0
            var column = 0
            while (input[index] != '*') {
                if (input[index] != '\n') {
                    line++
                    column = 0
                } else {
                    column++
                }
                index++
            }
            val inputCodepoints = input.toCodePoints()
            val mark = Mark(
                name = "testMarks",
                index = index,
                line = line,
                column = column,
                codepoints = inputCodepoints,
                pointer = index,
            )
            val snippet = mark.createSnippet(
                indentSize = 2, maxLength = 79,
            )
            withClue("Must contain at least one '\\n'.") {
                snippet shouldContain "\n"
            }
            withClue("Must only have only one '\n'.") {
                snippet.indexOf("\n") shouldBe snippet.lastIndexOf("\n")
            }
            val lines = snippet.split("\n")
            val data = lines[0]
            val pointer = lines[1]
            withClue("Mark must be restricted: $data") {
                data.length shouldBeLessThan 82
            }
            val dataPosition = data.indexOf("*")
            val pointerPosition = pointer.indexOf("^")
            withClue("Pointer should coincide with '*':\n $snippet") {
                dataPosition shouldBe pointerPosition
            }
        }
    }
})
