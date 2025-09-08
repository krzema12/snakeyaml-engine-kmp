package it.krzeminski.snakeyaml.engine.kmp.parser

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.scanner.ScannerImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

class ParserTest : FunSpec({
    test("expected NoSuchElementException after all the events are finished") {
        val settings = LoadSettings.builder().build()
        val reader = StreamReader(settings, "444333")
        val scanner = ScannerImpl(settings, reader)
        val parser = ParserImpl(settings, scanner)

        parser.hasNext() shouldBe true
        parser.next().eventId shouldBe Event.ID.StreamStart
        parser.hasNext() shouldBe true
        parser.next().eventId shouldBe Event.ID.DocumentStart
        parser.hasNext() shouldBe true
        parser.next().eventId shouldBe Event.ID.Scalar
        parser.hasNext() shouldBe true
        parser.next().eventId shouldBe Event.ID.DocumentEnd
        parser.hasNext() shouldBe true
        parser.next().eventId shouldBe Event.ID.StreamEnd
        parser.hasNext() shouldBe false

        shouldThrowWithMessage<NoSuchElementException>(message = "No more Events found.") {
            parser.next()
        }
    }
})
