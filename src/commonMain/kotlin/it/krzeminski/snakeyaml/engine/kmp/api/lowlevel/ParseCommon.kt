package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.Buffer
import okio.Source

internal class ParseCommon  (
    private val settings: LoadSettings,
) {
    fun parse(string: String): Iterable<Event> =
        parse(Buffer().writeUtf8(string))

    fun parse(source: Source): Iterable<Event> =
        Iterable {
            val reader = StreamReader(stream = source, loadSettings = settings)
            ParserImpl(settings, reader)
        }
}
