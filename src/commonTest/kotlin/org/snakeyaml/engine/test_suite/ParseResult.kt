package org.snakeyaml.engine.test_suite

import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.nodes.Node


internal data class ParseResult(
    val events: List<Event>,
    val error: Throwable? = null,
)


internal class ComposeResult(
    val node: List<Node>,
    val error: Exception? = null,
)
