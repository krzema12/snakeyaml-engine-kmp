package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.JsonScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver
import kotlin.jvm.JvmOverloads

open class JsonSchema @JvmOverloads constructor(
    override val scalarResolver: ScalarResolver = JsonScalarResolver(),
    override val schemaTagConstructors: Map<Tag, ConstructNode> = schemaTagConstructors(scalarResolver),
) : Schema

internal expect fun schemaTagConstructors(
    scalarResolver: ScalarResolver
): Map<Tag, ConstructNode>
