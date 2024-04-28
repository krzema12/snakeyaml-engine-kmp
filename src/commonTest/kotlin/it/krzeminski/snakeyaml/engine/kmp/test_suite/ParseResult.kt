package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.events.Event

internal data class ParseResult(
    val events: List<Event>?,
    val error: Throwable? = null,
)
