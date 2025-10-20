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
package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.internal.CopyDsl
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.env.EnvConfig
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlVersionException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.schema.DEFAULT_SCHEMA
import it.krzeminski.snakeyaml.engine.kmp.schema.Schema

/**
 * Immutable configuration for loading.
 */
@CopyDsl
class LoadSettings(
    /**
     * Label for the input data. Can be used to improve the error message.
     */
    val label: String = "reader",

    /**
     * Constructors for the specified tags.
     */
    val tagConstructors: Map<Tag, ConstructNode> = emptyMap(),

    /**
     * Default List implementation, as a function of its initial size.
     * [ArrayList] is used if nothing provided.
     */
    val defaultList: CollectionProvider<MutableList<Any?>> =
        CollectionProvider { initialCapacity: Int -> ArrayList(initialCapacity) },

    /**
     * Default Set implementation, as a function of its initial size.
     * [LinkedHashSet] is used if nothing provided.
     */
    val defaultSet: CollectionProvider<MutableSet<Any?>> =
        CollectionProvider { initialCapacity: Int -> LinkedHashSet(initialCapacity) },

    /**
     * Default Map implementation, as a function of its initial size.
     * [LinkedHashMap] is used if nothing provided.
     */
    val defaultMap: CollectionProvider<MutableMap<Any?, Any?>> =
        CollectionProvider { initialCapacity: Int -> LinkedHashMap(initialCapacity) },

    /**
     * Manage YAML directive value which defines the version of the YAML specification. This parser
     * supports YAML 1.2, but it can parse most of YAML 1.1 and YAML 1.0
     *
     * This function allows controlling the version management. For instance, if the document contains
     * an old version, the parser can be adapted to compensate for the problem. Or it can fail to indicate
     * that the incoming version is not supported.
     *
     * By default, 1.* version are accepted and treated as YAML 1.2. Other versions fail to parse
     * ([YamlVersionException] is thrown).
     */
    val versionFunction: SpecVersionMutator = SpecVersionMutator { version ->
        if (version.major != 1) throw YamlVersionException(version)
        version
    },

    /**
     * Buffer size for incoming data stream, in bytes. If the incoming stream is already buffered, then changing
     * the buffer does not improve the performance
     */
    val bufferSize: Int = 1024,

    /**
     * YAML 1.2 does require unique keys. To support the backwards compatibility, it is possible to
     * select what should happen when non-unique keys are detected.
     *
     * False by default.
     */
    val allowDuplicateKeys: Boolean = false,

    /**
     * Allow only non-recursive keys for maps and sets. By default, it is not allowed. Even though YAML
     * allows using anything as a key, it may cause unexpected issues when loading recursive structures.
     */
    val allowRecursiveKeys: Boolean = false,

    /**
     * Restrict the number of aliases for collection nodes to prevent 'billion laughs attack'. The
     * purpose of this setting is to force SnakeYAML to fail before a lot of CPU and memory resources
     * are allocated for the parser. Aliases for scalar nodes do not count because they do not grow
     * exponentially.
     *
     * 50 by default.
     */
    val maxAliasesForCollections: Int = 50,

    /**
     * Marks are only used for error messages, but they require a lot of memory. `true` by default.
     * Use false to save resources but use less informative error messages (no line and context).
     */
    val useMarks: Boolean = true,

    // general

    /**
     * Provide a custom property to be used later
     */
    val customProperties: Map<SettingKey, Any> = emptyMap(),

    /**
     * Define EnvConfig to parse ENV format. If not set explicitly, the variable substitution is not
     * applied.
     */
    val envConfig: EnvConfig? = null,

    /**
     * Parse comments to the presentation tree (Node). False by default.
     * Use true to parse comments to the presentation tree (Node)
     */
    val parseComments: Boolean = false,

    /**
     * The max number of code points for every input YAML document in the stream. Please be aware that
     * the byte limit depends on the encoding. The presence of the document indicators '---' or/and
     * '...' will affect the doc size (even though they do not belong to the document content)
     */
    val codePointLimit: Int = 3 * 1024 * 1024, // 3 MB

    /**
     * Provide either recommended or custom
     * [schema](https://yaml.org/spec/1.2.2/#chapter-10-recommended-schemas) instead of
     * default * [it.krzeminski.snakeyaml.engine.kmp.schema.DEFAULT_SCHEMA] These 3 are available
     * [it.krzeminski.snakeyaml.engine.kmp.schema.FailsafeSchema],
     * [it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema],
     * [it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema].
     *
     */
    val schema: Schema = DEFAULT_SCHEMA,
) {
    fun interface CollectionProvider<T> {
        operator fun invoke(initialCapacity: Int): T
    }

    fun interface SpecVersionMutator {
        operator fun invoke(version: SpecVersion): SpecVersion
    }
}
