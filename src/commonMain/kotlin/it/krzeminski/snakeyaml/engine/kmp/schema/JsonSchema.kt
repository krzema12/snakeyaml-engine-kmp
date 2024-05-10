package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructYamlNull
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlBinary
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonBool
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonFloat
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonInt
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.JsonScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver
import kotlin.jvm.JvmOverloads

open class JsonSchema @JvmOverloads constructor(
    override val scalarResolver: ScalarResolver = JsonScalarResolver(),
    override val schemaTagConstructors: Map<Tag, ConstructNode> = defaultSchemaTagConstructors(scalarResolver),
) : Schema

private fun defaultSchemaTagConstructors(scalarResolver: ScalarResolver): Map<Tag, ConstructNode> =
    buildMap {
        put(Tag.NULL, ConstructYamlNull())
        put(Tag.BOOL, ConstructYamlJsonBool())
        put(Tag.INT, ConstructYamlJsonInt())
        put(Tag.FLOAT, ConstructYamlJsonFloat())
        put(Tag.BINARY, ConstructYamlBinary())

        putAll(targetSchemaTagConstructors(scalarResolver))
    }

/**
 * Provides target-specific constructors for [JsonSchema.schemaTagConstructors].
 */
internal expect fun targetSchemaTagConstructors(
    scalarResolver: ScalarResolver
): Map<Tag, ConstructNode>
