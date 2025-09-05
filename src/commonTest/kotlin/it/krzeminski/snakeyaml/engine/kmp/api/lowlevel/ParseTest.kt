package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.compareEvents
import it.krzeminski.snakeyaml.engine.kmp.events.StreamEndEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent
import okio.Buffer

class ParseTest : FunSpec({
    test("parse empty string") {
        val parse = Parse(LoadSettings.builder().build())
        val events = parse.parse("")
        val list = events.toList()
        list.size shouldBe 2
        compareEvents(listOf(StreamStartEvent(), StreamEndEvent()), list)
    }

    test("parse empty buffer") {
        val parse = Parse(LoadSettings.builder().build())
        val events = parse.parse(Buffer())
        val list = events.toList()
        list.size shouldBe 2
        compareEvents(listOf(StreamStartEvent(), StreamEndEvent()), list)
    }
})
