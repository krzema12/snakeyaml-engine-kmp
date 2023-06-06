/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.schema

import org.snakeyaml.engine.v2.api.ConstructNode
import org.snakeyaml.engine.v2.constructor.ConstructYamlNull
import org.snakeyaml.engine.v2.constructor.json.ConstructOptionalClass
import org.snakeyaml.engine.v2.constructor.json.ConstructUuidClass
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlBinary
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonBool
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonFloat
import org.snakeyaml.engine.v2.constructor.json.ConstructYamlJsonInt
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.ScalarResolver

/**
 * Default schema for Kotlin/JVM
 */
actual open class JsonSchema actual constructor(
    final override val scalarResolver: ScalarResolver,
) : Schema {

    /** Basic constructs */
    override val schemaTagConstructors: Map<Tag, ConstructNode> = mapOf(
        Tag.NULL to ConstructYamlNull(),
        Tag.BOOL to ConstructYamlJsonBool(),
        Tag.INT to ConstructYamlJsonInt(),
        Tag.FLOAT to ConstructYamlJsonFloat(),
        Tag.BINARY to ConstructYamlBinary(),
        Tag.forType("java.util.UUID") to ConstructUuidClass(),
        Tag.forType("java.util.Optional") to ConstructOptionalClass(scalarResolver),
    )
}
