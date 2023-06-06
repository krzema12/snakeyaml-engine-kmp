package org.snakeyaml.engine.v2.representer

import org.snakeyaml.engine.v2.api.DumpSettings

/**
 * Provides the [Representer] for a specific target.
 */
expect fun Representer(settings: DumpSettings): Representer
