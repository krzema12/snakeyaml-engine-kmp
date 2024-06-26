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
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.FailsafeScalarResolver
import it.krzeminski.snakeyaml.engine.kmp.resolver.ScalarResolver

/**
 * The failsafe schema is guaranteed to work with any YAML document. It is therefore the recommended
 * schema for generic YAML tools.
 */
class FailsafeSchema : Schema {
    /**
     * Create [FailsafeScalarResolver]
     *
     * @return ScalarResolver which resolves everything as string
     */
    override val scalarResolver: ScalarResolver = FailsafeScalarResolver()

    /**
     * No constructs provided
     *
     * @return empty Map
     */
    override val schemaTagConstructors: Map<Tag, ConstructNode> = emptyMap()
}
