package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.events.Event

expect class Parse {
    fun parseString(yaml: String): Iterable<Event>
}
