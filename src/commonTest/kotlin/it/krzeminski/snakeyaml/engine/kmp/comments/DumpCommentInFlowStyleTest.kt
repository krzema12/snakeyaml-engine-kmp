package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Present
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

class DumpCommentInFlowStyleTest : FunSpec({
    test("ignoring comments") {
        val loader = Compose(LoadSettings.builder().setParseComments(false).build())
        val content = "{ url: text # comment breaks it\n}"
        val node = loader.compose(content)
        // check that no comment is present
        val textNode = (node as MappingNode).value.first().valueNode
        textNode.inLineComments.shouldBeEmpty()

        val serialize = Serialize(DumpSettings.builder().setDumpComments(true).build())
        val events = serialize.serializeOne(node)
        events.shouldHaveSize(8)
    }

    test("flow with comments") {
        val loader = Compose(LoadSettings.builder().setParseComments(true).build())
        val content = "{ url: text # comment breaks it\n}"
        val node = loader.compose(content)!!
        extractInlineComment(node) shouldBe " comment breaks it"

        val dumpSettings = DumpSettings.builder().setDumpComments(true).build()
        val serialize = Serialize(dumpSettings)
        val events = serialize.serializeOne(node)
        events.shouldHaveSize(9)

        shouldThrow<Exception> {
            val present = Present(dumpSettings)
            present.emitToString(events.iterator())
        }.also {
            it.message shouldContain "expected NodeEvent"
        }
    }

    test("block with comments") {
        val loader = Compose(LoadSettings.builder().setParseComments(true).build())
        val content = "url: text # comment breaks it\n"
        val node = loader.compose(content)!!

        extractInlineComment(node) shouldBe " comment breaks it"

        val dumpSettings = DumpSettings.builder().setDumpComments(true).build()
        val serialize = Serialize(dumpSettings)
        val events = serialize.serializeOne(node)
        events.shouldHaveSize(9)

        val present = Present(dumpSettings)
        val output = present.emitToString(events.iterator())
        output shouldBe content
    }
})

private fun extractInlineComment(node: Node): String =
    (node as MappingNode).value.first().valueNode.inLineComments!!.first().value
