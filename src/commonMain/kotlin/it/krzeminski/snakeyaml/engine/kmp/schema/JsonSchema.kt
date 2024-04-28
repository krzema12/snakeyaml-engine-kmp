package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.resolver.JsonScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver

expect open class JsonSchema(
    scalarResolver: ScalarResolver = JsonScalarResolver(),
) : Schema
