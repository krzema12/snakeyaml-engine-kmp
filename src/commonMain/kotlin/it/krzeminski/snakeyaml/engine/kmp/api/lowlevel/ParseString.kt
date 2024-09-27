package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event

@Deprecated("No longer used", ReplaceWith("it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse"))
class ParseString(
    settings: LoadSettings,
) {
    private val parse = ParseCommon(settings)

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param yaml - YAML document(s).
     * @return parsed events
     */
    fun parseString(yaml: String): Iterable<Event> = parse.parse(yaml)
}
