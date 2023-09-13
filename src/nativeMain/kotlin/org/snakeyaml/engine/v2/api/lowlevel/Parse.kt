package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.events.Event

actual class Parse actual constructor(
    settings: LoadSettings,
) {
    private val parseString = ParseString(settings)

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * @param yaml - YAML document(s). The BOM must not be present (it will be parsed as content)
     * @return parsed events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    actual fun parseString(yaml: String): Iterable<Event> =
        parseString.parseString(yaml)
}
