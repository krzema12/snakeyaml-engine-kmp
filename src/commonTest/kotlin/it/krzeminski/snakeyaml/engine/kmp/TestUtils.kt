package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.events.Event

fun compareEvents(expected: List<Event>, actual: List<Event>) {
    expected.size shouldBe actual.size
    expected.zip(actual).forEach { (expectedEvent, actualEvent) ->
        expectedEvent.toString() shouldBe actualEvent.toString()
    }
}
