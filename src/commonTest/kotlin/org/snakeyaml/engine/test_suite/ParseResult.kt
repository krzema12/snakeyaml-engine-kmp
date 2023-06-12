package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.v2.events.Event

internal data class ParseResult(
    val events: List<Event>,
    val error: Throwable? = null,
)
