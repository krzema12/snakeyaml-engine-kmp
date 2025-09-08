package it.krzeminski.snakeyaml.engine.kmp.nodes

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

class NodeTest : FunSpec({
    test("notEqualToTheSameNode") {
        val node1 = ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN)
        val node2 = ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN)
        node1 shouldNotBe node2 // "Nodes with the same content are not equal"
        node2 shouldNotBe node1 // "Nodes with the same content are not equal"
    }

    test("equalsToItself") {
        val node = ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN)
        node shouldBe node
    }

    test("properties") {
        val node = ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN)
        node.getProperty("p") shouldBe null
        node.setProperty("p", "value") shouldBe null
        node.getProperty("p") shouldBe "value"
    }
})
