package it.krzeminski.snakeyaml.engine.kmp.nodes

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class NodeTest : FunSpec({
    test("not equal to the same node") {
        val node1 = ScalarNode(tag = Tag.STR, value = "a", scalarStyle = ScalarStyle.PLAIN)
        val node2 = ScalarNode(tag = Tag.STR, value = "a", scalarStyle = ScalarStyle.PLAIN)
        node1 shouldNotBe node2 // "Nodes with the same content are not equal"
        node2 shouldNotBe node1 // "Nodes with the same content are not equal"
    }

    test("equals to itself") {
        val node = ScalarNode(tag = Tag.STR, value = "a", scalarStyle = ScalarStyle.PLAIN)
        node shouldBe node
    }

    test("properties") {
        val node = ScalarNode(tag = Tag.STR, value = "a", scalarStyle = ScalarStyle.PLAIN)
        node.getProperty("p") shouldBe null
        node.setProperty("p", "value") shouldBe null
        node.getProperty("p") shouldBe "value"
    }

    test("resolved by default") {
        val node: Node = ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN)
        node.isResolved() shouldBe true
    }

    test("not resolved when the tag is explicit") {
        val node: Node = ScalarNode(
            tag = Tag.STR, resolved = false, value = "a", scalarStyle = ScalarStyle.PLAIN,
            startMark = null, endMark = null,
        )
        node.isResolved() shouldBe false
    }
})
