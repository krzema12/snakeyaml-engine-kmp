package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.events.Event
import okio.Source

expect class Parse {
    /**
     * Parse a YAML string and produce parsing events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param string YAML document(s). The BOM must not be present (it will be parsed as content)
     * @return parsed events
     */
    fun parse(string: String): Iterable<Event>

    /**
     * Parse a YAML stream and produce parsing events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param source YAML document(s). The BOM must not be present (it will be parsed as content)
     * @return parsed events
     */
    fun parse(source: Source): Iterable<Event>

    @Deprecated("renamed", ReplaceWith("parse(yaml)"))
    fun parseString(yaml: String): Iterable<Event>
}
