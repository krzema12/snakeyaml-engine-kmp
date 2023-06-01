package org.snakeyaml.engine.v2.schema

import org.snakeyaml.engine.v2.api.ConstructNode
import org.snakeyaml.engine.v2.constructor.ConstructYamlNull
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlBinary
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonBool
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonInt
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver
import org.snakeyaml.engine.v2.resolver.ScalarResolver

/**
 * Default schema for Kotlin/JS
 */
actual open class JsonSchema(
    override val scalarResolver: ScalarResolver = JsonScalarResolver(),
    /** Basic constructs */
    override val schemaTagConstructors: Map<Tag, ConstructNode> = mapOf(
        Tag.NULL to ConstructYamlNull(),
        Tag.BOOL to ConstructYamlJsonBool(),
        Tag.INT to ConstructYamlJsonInt(),
        Tag.FLOAT to ConstructYamlJsonFloat(),
        Tag.BINARY to ConstructYamlBinary(),
    ),
) : Schema
