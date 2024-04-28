package it.krzeminski.snakeyaml.engine.kmp.representer

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings

/**
 * Provides the [Representer] for a specific target.
 */
expect fun Representer(settings: DumpSettings): Representer
