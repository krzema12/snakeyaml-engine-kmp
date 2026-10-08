package it.krzeminski.snakeyaml.engine.kmp.issues.issue99

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * Ported from issue 99 in SnakeYAML Engine.
 *
 * YAML 1.2 forbids TAB in indentation only. TAB is legal separation whitespace between
 * tokens on a line, also in block context. It still may not be used where it would act as
 * indentation, i.e. in front of a block collection indicator or an implicit key which starts a new
 * block mapping (see Y79Y in the YAML test suite).
 */
class TabSeparationTest : FunSpec({
    fun load(yaml: String): Any? = Load(LoadSettings()).loadOne(yaml)

    fun assertFails(yaml: String) {
        val e = shouldThrow<YamlEngineException> { load(yaml) }
        withClue("Error must mention TAB: ${e.message}") {
            (e.message!!.contains("TAB") || e.message!!.contains("\\t")) shouldBe true
        }
    }

    test("TAB after the ':' value indicator") {
        load("a:\tvalue") shouldBe mapOf("a" to "value")
        load("a: \tvalue") shouldBe mapOf("a" to "value")
        load("a:\t \tvalue") shouldBe mapOf("a" to "value")
        load("a:\n  b:\tc") shouldBe mapOf("a" to mapOf("b" to "c"))
    }

    test("TAB after the '-' block entry indicator") {
        load("-\tx") shouldBe listOf("x")
        load("- \tx") shouldBe listOf("x")
        load("-\tx\n-\ty") shouldBe listOf("x", "y")
    }

    test("TAB after explicit key and value indicators") {
        load("? a\n:\tb") shouldBe mapOf("a" to "b")
        load("?\ta\n:\tb") shouldBe mapOf("a" to "b")
    }

    test("TAB before flow collections and quoted scalars") {
        load("a:\t[1, 2]") shouldBe mapOf("a" to listOf(1, 2))
        load("a:\t{b: 1}") shouldBe mapOf("a" to mapOf("b" to 1))
        load("a:\t\"q\"") shouldBe mapOf("a" to "q")
        load("a:\t'q'") shouldBe mapOf("a" to "q")
        load("\"a\"\t: b") shouldBe mapOf("a" to "b")
    }

    test("TAB after the document start marker") {
        load("---\tx") shouldBe "x"
        load("--- \tx") shouldBe "x"
    }

    test("TAB after anchors and tags") {
        load("a: &x\t1") shouldBe mapOf("a" to 1)
        load("a: !!str\t1") shouldBe mapOf("a" to "1")
        load("a: !!str\t&x\t1") shouldBe mapOf("a" to "1")
    }

    test("TAB inside directives") {
        load("%YAML\t1.2\t# c\n---\ta") shouldBe "a"
        load("%TAG\t!e!\ttag:yaml.org,2002:\n--- !e!str\ta") shouldBe "a"
    }

    test("TAB around block scalar headers") {
        load("a:\t|\n  x\n") shouldBe mapOf("a" to "x\n")
        load("a: |\t# c\n  x\n") shouldBe mapOf("a" to "x\n")
        load("a: |\t\n  x\n") shouldBe mapOf("a" to "x\n")
    }

    test("TAB before a plain scalar that only looks like an indicator") {
        load("-\t-1") shouldBe listOf(-1)
        load("? a\n: -\tb") shouldBe mapOf("a" to listOf("b"))
    }

    test("TAB and space mixed in flow context") {
        load("a: {b:\t c}\n") shouldBe mapOf("a" to mapOf("b" to "c"))
        load("a: [1,\t 2]") shouldBe mapOf("a" to listOf(1, 2))
        load("a: [1, \t \t2]") shouldBe mapOf("a" to listOf(1, 2))
        load("{\n\t \"a\": 1\n}") shouldBe mapOf("a" to 1)
    }

    test("TAB may not precede a block collection indicator") {
        assertFails("-\t-")
        assertFails("- \t-")
        assertFails("?\t-")
        assertFails("? -\n:\t-")
        // invalid also with a space instead of TAB
        shouldThrow<YamlEngineException> { load("a:\t- x") }
    }

    test("TAB may not precede an implicit key which starts a new block mapping") {
        assertFails("?\tkey:")
        assertFails("? key:\n:\tkey:")
        assertFails("-\ta: b")
    }

    test("TAB as indentation is still rejected") {
        assertFails("a:\n\tb: c")
        // Divergence from upstream: upstream reports this input with a TAB-specific message. KMP still
        // rejects it, but with a different message, because KMP's scanPlainSpaces also skips TABs on
        // continuation lines (KMP-only change, PR #173), which is what lets the YAML Test Suite cases
        // UV7Q, NB6Z and HS5T pass. If scanPlainSpaces is ever aligned with upstream (skipping only spaces
        // on continuation lines), this assertion should go back to assertFails(...) as upstream has it.
        shouldThrow<ScannerException> { load("foo:\n  a: 1\n  \tb: 2") }
            .message.shouldContain("mapping values are not allowed here").shouldContain("line 3, column 5")
    }
})
