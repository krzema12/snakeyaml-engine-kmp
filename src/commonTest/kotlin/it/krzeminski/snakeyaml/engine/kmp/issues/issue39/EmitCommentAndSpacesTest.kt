package it.krzeminski.snakeyaml.engine.kmp.issues.issue39

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StringStreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.parser.Parser
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class EmitCommentAndSpacesTest : FunSpec({
    test("Issue 39: extra space added") {
        val loadSettings = LoadSettings.builder().setParseComments(true).build()
        val input = stringFromResources("issues/issue39-input.yaml")
        val parser = ParserImpl(loadSettings, StreamReader(loadSettings, input))
        val settings = DumpSettings.builder().setDumpComments(true).build()
        val writer = StringStreamDataWriter()
        val emitter = Emitter(settings, writer)
        while (parser.hasNext()) {
            val event = parser.next()
            emitter.emit(event)
        }
        writer.toString() shouldNotBe input
    }

    test("Issue 39: extra space added - small example") {

        val loadSettings = LoadSettings.builder().setParseComments(true).build()
        val input = """first:
  second: abc



"""
        val parser: Parser = ParserImpl(loadSettings, StreamReader(loadSettings, input))
        val settings: DumpSettings = DumpSettings.builder().setDumpComments(true).build()
        val writer = StringStreamDataWriter()
        val emitter = Emitter(settings, writer)
        val events = mutableListOf<Event>()
        while (parser.hasNext()) {
            val event = parser.next()
            events.add(event)
            emitter.emit(event)
        }
        events.size shouldBe 14
        (events[6] as ScalarEvent).value shouldBe "abc"
    }
})
