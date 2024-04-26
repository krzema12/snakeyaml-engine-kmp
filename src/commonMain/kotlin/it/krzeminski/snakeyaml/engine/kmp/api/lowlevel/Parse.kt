package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event

expect class Parse(
    settings: LoadSettings
) {
    fun parseString(yaml: String): Iterable<Event>
}
