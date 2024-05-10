package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver

/** Basic constructs for Kotlin/Native */
internal actual fun targetSchemaTagConstructors(
    scalarResolver: ScalarResolver
): Map<Tag, ConstructNode> = emptyMap()
