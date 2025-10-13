package it.krzeminski.snakeyaml.engine.kmp.issues.issue544

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Present
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeTuple
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class DoubleQuoteTest : FunSpec({
    test("unicode substitution") {
        val settings = DumpSettings(isUseUnicodeEncoding = false)
        val expectedOutput = "double_quoted: \"\\U0001f510This process is simple and secure.\"\n" +
                "single_quoted: \"\\U0001f510This process is simple and secure.\"\n"
        emit(settings) shouldBe expectedOutput
    }

    test("unicode encoding") {
        val settings = DumpSettings(isUseUnicodeEncoding = true)
        val expectedOutput = "double_quoted: \"🔐This process is simple and secure.\"\n" +
                "single_quoted: '🔐This process is simple and secure.'\n"
        emit(settings) shouldBe expectedOutput
    }

    test("default settings") {
        val settings = DumpSettings()
        val expectedOutput = "double_quoted: \"🔐This process is simple and secure.\"\n" +
                "single_quoted: '🔐This process is simple and secure.'\n"
        emit(settings) shouldBe expectedOutput
    }
})

private fun emit(settings: DumpSettings): String {
    val serialize = Serialize(settings)
    val eventsIter = serialize.serializeOne(createMappingNode())
    val emit = Present(settings)
    return emit.emitToString(eventsIter.iterator())
}

private fun createMappingNode(): MappingNode {
    val content = "🔐This process is simple and secure."

    val doubleQuotedKey = ScalarNode(Tag.STR, "double_quoted", ScalarStyle.PLAIN)
    val doubleQuotedValue = ScalarNode(Tag.STR, content, ScalarStyle.DOUBLE_QUOTED)
    val doubleQuotedTuple = NodeTuple(doubleQuotedKey, doubleQuotedValue)

    val singleQuotedKey = ScalarNode(Tag.STR, "single_quoted", ScalarStyle.PLAIN)
    val singleQuotedValue = ScalarNode(Tag.STR, content, ScalarStyle.SINGLE_QUOTED)
    val singleQuotedTuple = NodeTuple(singleQuotedKey, singleQuotedValue)

    val nodeTuples = mutableListOf<NodeTuple>()
    nodeTuples.add(doubleQuotedTuple)
    nodeTuples.add(singleQuotedTuple)

    return MappingNode(Tag.MAP, nodeTuples, FlowStyle.BLOCK)
}
