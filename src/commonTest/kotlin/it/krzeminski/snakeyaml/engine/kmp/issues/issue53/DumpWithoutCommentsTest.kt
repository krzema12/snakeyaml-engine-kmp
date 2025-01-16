package it.krzeminski.snakeyaml.engine.kmp.issues.issue53

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StringStreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.serializer.Serializer

/**
 * Issue 53.
 */
class DumpWithoutCommentsTest : FunSpec({
    test("dump map with comments") {
        val yaml = "a: 1 # A\nb: 2 # B\n"
        val dumpSettings = DumpSettings.builder().setDumpComments(false).build()

        val streamWriter = StringStreamDataWriter()
        val emitter = Emitter(
            dumpSettings,
            streamWriter,
        )
        val serializer = Serializer(dumpSettings, emitter)

        serializer.emitStreamStart()
        serializer.serializeDocument(createNodeWithComments(yaml))
        serializer.emitStreamEnd()

        streamWriter.toString() shouldBe "a: 1\nb: 2\n"
    }

    test("check no comments") {
        val source = "a: 1 # comment"
        val dumpSettings = DumpSettings.builder().setDumpComments(false).build()
        val serializer = Serialize(dumpSettings)
        val events  = serializer.serializeOne(createNodeWithComments(source))

        val commentEvents = events
            .filter { e: Event -> e.eventId == Event.ID.Comment }
            .toList()
        commentEvents shouldBe emptyList()
    }
})

fun createNodeWithComments(source: String): Node {
    val loadSettings = LoadSettings.builder().setParseComments(true).build()
    val parser = ParserImpl(loadSettings, StreamReader(loadSettings, source))
    val composer = Composer(loadSettings, parser)
    return composer.getSingleNode()!!
}
