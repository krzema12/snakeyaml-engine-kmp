package org.snakeyaml.engine.v2.schema

import org.snakeyaml.engine.v2.resolver.JsonScalarResolver
import org.snakeyaml.engine.v2.resolver.ScalarResolver

expect open class JsonSchema(
    scalarResolver: ScalarResolver = JsonScalarResolver(),
) : Schema
