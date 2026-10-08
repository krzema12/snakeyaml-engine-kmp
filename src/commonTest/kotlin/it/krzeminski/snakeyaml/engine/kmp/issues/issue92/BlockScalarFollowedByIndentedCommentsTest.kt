package it.krzeminski.snakeyaml.engine.kmp.issues.issue92

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Present
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.events.CommentEvent
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode

/**
 * Ported from issue 92 in SnakeYAML Engine.
 *
 * A block scalar (| or >) nested inside a collection, followed by two or more indented
 * comment lines separated by a blank line, used to make the scanner misclassify those whole-line
 * comments as in-line comments. The stray comment event then leaked into the composer, which threw
 * an unchecked ClassCastException when comment parsing was enabled.
 */
class BlockScalarFollowedByIndentedCommentsTest : FunSpec({
    val settings = LoadSettings(parseComments = true)

    fun commentTypes(yaml: String): List<CommentType> =
        Parse(settings).parse(yaml).filterIsInstance<CommentEvent>().map { it.commentType }

    fun foo(loaded: Any?): Any? = ((loaded as Map<*, *>)["cm"] as Map<*, *>)["foo"]

    test("literal block scalar followed by two indented comments loads as expected") {
        val yaml = "cm:\n  foo: |\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        Load(settings).loadOne(yaml) shouldBe mapOf("cm" to mapOf("foo" to "x\n", "bar" to 1))
    }

    test("the indented comments are attached as block comments, not dropped") {
        val yaml = "cm:\n  foo: |\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        val root = Compose(settings).compose(yaml).shouldNotBeNull() as MappingNode
        val tuples = (root.value[0].valueNode as MappingNode).value
        tuples.size shouldBe 2

        val barKey = tuples[1].keyNode as ScalarNode
        barKey.value shouldBe "bar"
        val blockComments = barKey.blockComments.shouldNotBeNull()
        blockComments.size shouldBe 3
        blockComments[0].commentType shouldBe CommentType.BLOCK
        blockComments[0].value shouldBe " comment 1"
        blockComments[1].commentType shouldBe CommentType.BLANK_LINE
        blockComments[2].commentType shouldBe CommentType.BLOCK
        blockComments[2].value shouldBe " comment 2"
    }

    test("folded block scalar variant") {
        val yaml = "cm:\n  foo: >\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        foo(Load(settings).loadOne(yaml)) shouldBe "x\n"
    }

    test("explicit chomping indicator variant") {
        val yaml = "cm:\n  foo: |-\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        foo(Load(settings).loadOne(yaml)) shouldBe "x"
    }

    test("block scalar inside a sequence item variant") {
        val yaml = "- foo: |\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        Load(settings).loadOne(yaml) shouldBe listOf(mapOf("foo" to "x\n", "bar" to 1))
    }

    test("'#' lines indented to the block scalar's own content level are literal content, not comments - only dedented lines are") {
        val yaml = "cm:\n  foo: |\n    x\n    # comment 1\n\n    # comment 2\n  bar: 1\n"
        Load(settings).loadOne(yaml) shouldBe
            mapOf("cm" to mapOf("foo" to "x\n# comment 1\n\n# comment 2\n", "bar" to 1))
        commentTypes(yaml) shouldBe emptyList()
    }

    test("a single indented comment is a block comment on the next key, not an in-line comment on the block scalar") {
        val yaml = "cm:\n  foo: |\n    x\n  # comment\n  bar: 1\n"
        val root = Compose(settings).compose(yaml).shouldNotBeNull() as MappingNode
        val cm = root.value[0].valueNode as MappingNode

        cm.value[0].valueNode.inLineComments shouldBe emptyList()
        val comments = cm.value[1].keyNode.blockComments.shouldNotBeNull()
        comments[0].value shouldBe " comment"
        comments.size shouldBe 1
        comments[0].commentType shouldBe CommentType.BLOCK
    }

    test("indented comments at the end of the stream variant") {
        val yaml = "cm:\n  foo: |\n    x\n  # comment 1\n\n  # comment 2\n"
        foo(Load(settings).loadOne(yaml)) shouldBe "x\n"
    }

    test("indented comments at the end of the stream variant (not comments)") {
        val yaml = "cm:\n  foo: |\n    x\n    # comment 1\n\n    # comment 2\n  bar: 1"
        foo(Load(settings).loadOne(yaml)) shouldBe "x\n# comment 1\n\n# comment 2\n"
    }

    test("CRLF line breaks - the classification hinges on the column the reader reports after a line break") {
        val yaml = "cm:\r\n  foo: |\r\n    x\r\n  # comment 1\r\n\r\n  # comment 2\r\n  bar: 1\r\n"
        Load(settings).loadOne(yaml) shouldBe mapOf("cm" to mapOf("foo" to "x\n", "bar" to 1))
        commentTypes(yaml) shouldBe
            listOf(CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK)
    }

    test("a genuine in-line comment on the block scalar header stays IN_LINE while the trailing indented comments become BLOCK") {
        val yaml = "cm:\n  foo: | # inline\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        commentTypes(yaml) shouldBe listOf(
            CommentType.IN_LINE, CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK,
        )

        val root = Compose(settings).compose(yaml).shouldNotBeNull() as MappingNode
        val cm = root.value[0].valueNode as MappingNode

        val inLineComments = cm.value[0].keyNode.inLineComments.shouldNotBeNull()
        inLineComments.size shouldBe 1
        inLineComments[0].commentType shouldBe CommentType.IN_LINE
        inLineComments[0].value shouldBe " inline"

        val blockComments = cm.value[1].keyNode.blockComments.shouldNotBeNull()
        blockComments.size shouldBe 3
        blockComments[0].value shouldBe " comment 1"
        blockComments[1].commentType shouldBe CommentType.BLANK_LINE
        blockComments[2].value shouldBe " comment 2"
    }

    test("the reproducer round trips - the comments are not merely present but positioned so that the emitter reproduces the input exactly") {
        val yaml = "cm:\n  foo: |\n    x\n  # comment 1\n\n  # comment 2\n  bar: 1\n"
        val node = Compose(settings).compose(yaml).shouldNotBeNull()

        val dumpSettings = DumpSettings(dumpComments = true)
        val events = Serialize(dumpSettings).serializeOne(node)
        Present(dumpSettings).emitToString(events.iterator()) shouldBe yaml
    }
})
