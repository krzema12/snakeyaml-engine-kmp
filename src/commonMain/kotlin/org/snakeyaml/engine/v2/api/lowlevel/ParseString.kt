package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader

class ParseString(
    private val settings: LoadSettings,
) {

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @param yaml - YAML document(s). The BOM must not be present (it will be parsed as content)
     * @return parsed events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun parseString(yaml: String): Iterable<Event> =
        ParserImpl(settings, StreamReader(settings, yaml))
            .asSequence().asIterable()
}
