package it.krzeminski.snakeyaml.engine.kmp.test_suite

import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Parse
import it.krzeminski.snakeyaml.engine.kmp.events.Event

/**
 * Parse a single test case from [YAML Test Suite](https://github.com/yaml/yaml-test-suite)
 * passed as [data].
 */
fun parseTestData(data: YamlTestData): ParseResult {
    val settings = LoadSettings(label = data.label)

    val events = mutableListOf<Event>()
    return runCatching {
        Parse(settings).parse(data.inYaml).forEach(events::add)
        ParseResult(events)
    }.recover {
        ParseResult(events, it)
    }.getOrThrow()
}
