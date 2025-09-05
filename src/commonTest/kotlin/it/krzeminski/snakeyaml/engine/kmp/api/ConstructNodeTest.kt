package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class ConstructNodeTest : FunSpec({
    test("fail to construct recursive") {
        val constructNode = object : TestConstructNode() {
            override fun construct(node: Node?): Any? {
                return null
            }
        }
        val node = SequenceNode(
            Tag.SEQ,
            listOf(ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)),
            FlowStyle.FLOW
        )
        node.isRecursive = true
        val exception = shouldThrow<IllegalStateException> {
            constructNode.constructRecursive(node, ArrayList<Any>())
        }
        exception.message shouldBe "Not implemented in class ${constructNode::class.qualifiedName}"
    }

    test("fail to construct non recursive") {
        val constructNode = object : TestConstructNode() {
            override fun construct(node: Node?): Any? {
                return null
            }
        }
        val node = SequenceNode(
            Tag.SEQ,
            listOf(ScalarNode(Tag.STR, "b", ScalarStyle.PLAIN)),
            FlowStyle.FLOW
        )
        node.isRecursive = false
        val exception = shouldThrow<YamlEngineException> {
            constructNode.constructRecursive(node, ArrayList<Any>())
        }
        exception.message!! shouldStartWith "Unexpected recursive structure for Node"
    }
})

private abstract class TestConstructNode : ConstructNode {
    override fun constructRecursive(node: Node, `object`: Any) {
        if (node.isRecursive) {
            throw IllegalStateException("Not implemented in class ${this::class.qualifiedName}")
        }
        throw YamlEngineException("Unexpected recursive structure for Node $node")
    }
}
