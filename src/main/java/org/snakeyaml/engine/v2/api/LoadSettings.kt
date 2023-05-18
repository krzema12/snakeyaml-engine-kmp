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
package org.snakeyaml.engine.v2.api

import org.snakeyaml.engine.v2.common.SpecVersion
import org.snakeyaml.engine.v2.env.EnvConfig
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.schema.Schema
import java.util.Optional
import java.util.function.Function
import java.util.function.IntFunction
import java.util.function.UnaryOperator

/**
 * Immutable configuration for loading. Description for all the fields can be found in the builder
 */
class LoadSettings internal constructor(
  @JvmField val label: String, @JvmField val tagConstructors: Map<Tag, ConstructNode>,
  @JvmField val defaultList: IntFunction<List<Any>>, @JvmField val defaultSet: IntFunction<Set<Any>>,
  @JvmField val defaultMap: IntFunction<Map<Any, Any>>, private val versionFunction: UnaryOperator<SpecVersion>,
  @JvmField val bufferSize: Int, @JvmField val allowDuplicateKeys: Boolean, @JvmField val allowRecursiveKeys: Boolean,
  @JvmField val maxAliasesForCollections: Int, @JvmField val useMarks: Boolean, // general
  private val customProperties: Map<SettingKey, Any>,
  @JvmField val envConfig: Optional<EnvConfig>, @JvmField val parseComments: Boolean, @JvmField val codePointLimit: Int, @JvmField val schema: Schema
) {

    fun getVersionFunction(): Function<SpecVersion, SpecVersion> {
        return versionFunction
    }

    fun getCustomProperty(key: SettingKey): Any? {
        return customProperties[key]
    }

    companion object {
        /**
         * Create the builder
         *
         * @return the builder to fill the configuration options
         */
        @JvmStatic
        fun builder(): LoadSettingsBuilder {
            return LoadSettingsBuilder()
        }
    }
}