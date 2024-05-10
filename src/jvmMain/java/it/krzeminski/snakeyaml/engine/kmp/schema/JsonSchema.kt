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
package it.krzeminski.snakeyaml.engine.kmp.schema

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.constructor.ConstructYamlNull
import it.krzeminski.snakeyaml.engine.kmp.constructor.json.*
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver

/** Basic constructs */
internal actual fun schemaTagConstructors(
    scalarResolver: ScalarResolver
): Map<Tag, ConstructNode> =
    mapOf(
        Tag.NULL to ConstructYamlNull(),
        Tag.BOOL to ConstructYamlJsonBool(),
        Tag.INT to ConstructYamlJsonInt(),
        Tag.FLOAT to ConstructYamlJsonFloat(),
        Tag.BINARY to ConstructYamlBinary(),
        Tag.forType("java.util.UUID") to ConstructUuidClass(),
        Tag.forType("java.util.Optional") to ConstructOptionalClass(scalarResolver),
    )
