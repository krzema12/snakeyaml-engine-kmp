package it.krzeminski.snakeyaml.engine.kmp.issues.issue97

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Present
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode

/**
 * Ported from issue 97 in SnakeYAML Engine.
 *
 * A block scalar (| or >) used as a sequence entry, with a comment on its header line,
 * has no preceding node to absorb that comment - unlike the equivalent mapping value, where the key
 * absorbs it. The composer threw an unchecked ClassCastException (CommentEvent cannot be cast to
 * NodeEvent) when comment parsing was enabled.
 */
class BlockScalarAsSequenceEntryWithCommentTest : FunSpec({
    val settings = LoadSettings(parseComments = true)

    test("folded block scalar as a sequence entry with a header comment loads") {
        val yaml = "- > # c\n  text\n"
        Load(settings).loadOne(yaml) shouldBe listOf("text\n")
    }

    test("literal block scalar as a sequence entry with a header comment loads") {
        val yaml = "- | # c\n  text\n"
        Load(settings).loadOne(yaml) shouldBe listOf("text\n")
    }

    test("strip chomping variant") {
        val yaml = "- >- # c\n  text\n"
        Load(settings).loadOne(yaml) shouldBe listOf("text")
    }

    test("keep chomping variant") {
        val yaml = "- >+ # c\n  text\n\n"
        Load(settings).loadOne(yaml) shouldBe listOf("text\n\n")
    }

    test("empty block scalar as a sequence entry with a header comment") {
        val yaml = "- > # c\n"
        Load(settings).loadOne(yaml) shouldBe listOf("")
    }

    test("block scalar as an entry of a sequence nested inside a mapping value") {
        val yaml = "outer:\n  - > # c\n    text\n"
        Load(settings).loadOne(yaml) shouldBe mapOf("outer" to listOf("text\n"))
    }

    test("the header comment is attached to the block scalar's own node, not lost") {
        val yaml = "- > # c\n  text\n"
        val node = Compose(settings).compose(yaml)
        node.shouldNotBeNull()

        val root = node as SequenceNode
        val entry = root.value[0] as ScalarNode
        val inLineComments = entry.inLineComments
        inLineComments.shouldNotBeNull()
        inLineComments.size shouldBe 1
        inLineComments[0].value shouldBe " c"
    }

    test(
        "regression - a block scalar mapping value still lets the key absorb the " +
            "header comment, unaffected by the sequence-entry fix",
    ) {
        val yaml = "k: > # c\n  text\n"
        val node = Compose(settings).compose(yaml)
        node.shouldNotBeNull()

        val root = node as MappingNode
        val key = root.value[0].keyNode as ScalarNode
        val value = root.value[0].valueNode as ScalarNode
        val keyComments = key.inLineComments
        keyComments.shouldNotBeNull()
        keyComments.size shouldBe 1
        keyComments[0].value shouldBe " c"
        value.inLineComments shouldBe emptyList()
    }

    test(
        "an explicit block key that is itself a block scalar with a header " +
            "comment round-trips - the comment must not end up orphaning the value",
    ) {
        val yaml = "? !!str | # c\n  text\n: v\n"
        val node = Compose(settings).compose(yaml)
        node.shouldNotBeNull()

        val dumpSettings = DumpSettings(dumpComments = true)
        val events = Serialize(dumpSettings).serializeOne(node)
        val dumped = Present(dumpSettings).emitToString(events.iterator())

        Load(settings).loadOne(dumped) shouldBe mapOf("text\n" to "v")
    }

    test(
        "a standalone comment between an explicit key's node and the ':' " +
            "indicator loads instead of breaking the block mapping",
    ) {
        val yaml = "? |\n  text\n# c\n: v\n"
        Load(settings).loadOne(yaml) shouldBe mapOf("text\n" to "v")
    }
})
