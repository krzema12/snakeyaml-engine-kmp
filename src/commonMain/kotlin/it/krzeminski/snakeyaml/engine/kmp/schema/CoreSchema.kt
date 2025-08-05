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
import it.krzeminski.snakeyaml.engine.kmp.constructor.core.ConstructYamlCoreBool
import it.krzeminski.snakeyaml.engine.kmp.constructor.core.ConstructYamlCoreFloat
import it.krzeminski.snakeyaml.engine.kmp.constructor.core.ConstructYamlCoreInt
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.CoreScalarResolver

/**
 * Core schema
 */
class CoreSchema : JsonSchema(CoreScalarResolver(supportMerge = true)) {

    /**
     * Provide constructs to support the schema (bool, int, float)
     */
    override val schemaTagConstructors: Map<Tag, ConstructNode> = super.schemaTagConstructors + tagConstructors

    companion object {
        private val tagConstructors: Map<Tag, ConstructNode> = mapOf(
            Tag.BOOL to ConstructYamlCoreBool(),
            Tag.INT to ConstructYamlCoreInt(),
            Tag.FLOAT to ConstructYamlCoreFloat(),
        )
    }
}
