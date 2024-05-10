package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructYamlNull
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlBinary
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonBool
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonFloat
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.ConstructYamlJsonInt
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver

internal actual fun schemaTagConstructors(
    scalarResolver: ScalarResolver
): Map<Tag, ConstructNode> =
    mapOf(
        Tag.NULL to ConstructYamlNull(),
        Tag.BOOL to ConstructYamlJsonBool(),
        Tag.INT to ConstructYamlJsonInt(),
        Tag.FLOAT to ConstructYamlJsonFloat(),
        Tag.BINARY to ConstructYamlBinary(),
    )
