package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import okio.Source

actual class Parse actual constructor(
    settings: LoadSettings,
) {
    private val common = ParseCommon(settings)

    actual fun parse(string: String): Iterable<Event> = common.parse(string)

    actual fun parse(source: Source): Iterable<Event> = common.parse(source)

    @Deprecated("renamed", ReplaceWith("parse(yaml)"))
    actual fun parseString(yaml: String): Iterable<Event> = parse(yaml)
}
