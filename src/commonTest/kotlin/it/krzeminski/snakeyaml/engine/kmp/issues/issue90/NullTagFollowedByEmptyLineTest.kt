package it.krzeminski.snakeyaml.engine.kmp.issues.issue90

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

/**
 * Issue 90: a tagged empty scalar (e.g. !!null) followed by a blank line used to leave the
 * enclosing block mapping unterminated when comment parsing was enabled, so the next key was
 * mistaken for the start of a new document.
 */
class NullTagFollowedByEmptyLineTest : FunSpec({
    val settings = LoadSettings(parseComments = true)
    val yaml = "a: !!null\n\nb: 1\n"

    test("Issue 90: !!null followed by an empty line does not break the mapping") {
        val node = Compose(settings).compose(yaml).shouldNotBeNull()

        val tuples = (node as MappingNode).value
        tuples.size shouldBe 2

        val keyA = tuples[0].keyNode as ScalarNode
        val valueA = tuples[0].valueNode as ScalarNode
        keyA.value shouldBe "a"
        valueA.tag shouldBe Tag.NULL

        val keyB = tuples[1].keyNode as ScalarNode
        val valueB = tuples[1].valueNode as ScalarNode
        keyB.value shouldBe "b"
        valueB.value shouldBe "1"
    }

    test("Issue 90: !!null followed by an empty line loads as expected") {
        val loaded = Load(settings).loadOne(yaml)

        loaded shouldBe mapOf("a" to null, "b" to 1)
        (loaded as Map<*, *>)["a"].shouldBeNull()
    }
})
