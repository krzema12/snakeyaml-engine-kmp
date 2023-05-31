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

import org.snakeyaml.engine.v2.api.LoadSettings.CollectionProvider
import org.snakeyaml.engine.v2.api.LoadSettings.SpecVersionMutator
import org.snakeyaml.engine.v2.env.EnvConfig
import org.snakeyaml.engine.v2.exceptions.YamlVersionException
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.schema.JsonSchema
import org.snakeyaml.engine.v2.schema.Schema
import java.util.ArrayList

/**
 * Builder pattern implementation for LoadSettings
 */
class LoadSettingsBuilder internal constructor() {
    private val customProperties: MutableMap<SettingKey, Any> = HashMap()
    private var label = "reader"
    private var tagConstructors: Map<Tag, ConstructNode> = mutableMapOf()
    private var defaultList: CollectionProvider<MutableList<Any?>> =
        CollectionProvider { initialCapacity: Int -> ArrayList(initialCapacity) }
    private var defaultSet: CollectionProvider<MutableSet<Any?>> =
        CollectionProvider { initialCapacity: Int -> LinkedHashSet(initialCapacity) }
    private var defaultMap: CollectionProvider<MutableMap<Any?, Any?>> =
        CollectionProvider { initialCapacity: Int -> LinkedHashMap(initialCapacity) }
    private var versionFunction: SpecVersionMutator =
        SpecVersionMutator { version ->
            if (version.major != 1) throw YamlVersionException(version)
            version
        }
    private var bufferSize: Int = 1024
    private var allowDuplicateKeys: Boolean = false
    private var allowRecursiveKeys: Boolean = false
    private var parseComments: Boolean = false

    /** to prevent YAML at https://en.wikipedia.org/wiki/Billion_laughs_attack */
    private var maxAliasesForCollections: Int = 50
    private var useMarks: Boolean = true
    private var envConfig: EnvConfig? = null // no ENV substitution by default
    private var codePointLimit: Int = 3 * 1024 * 1024 // 3 MB
    private var schema: Schema = JsonSchema()

    /**
     * Label for the input data. Can be used to improve the error message.
     *
     * @param label - meaningful label to indicate the input source
     * @return the builder with the provided value
     */
    fun setLabel(label: String): LoadSettingsBuilder {
        this.label = label
        return this
    }

    /**
     * Provide constructors for the specified tags.
     *
     * @param tagConstructors - the map from a Tag to its constructor
     * @return the builder with the provided value
     */
    fun setTagConstructors(tagConstructors: Map<Tag, ConstructNode>): LoadSettingsBuilder {
        this.tagConstructors = tagConstructors
        return this
    }

    /**
     * Provide default List implementation. [ArrayList] is used if nothing provided.
     *
     * @param defaultList - specified List implementation (as a function from init size)
     * @return the builder with the provided value
     */
    fun setDefaultList(defaultList: CollectionProvider<MutableList<Any?>>): LoadSettingsBuilder {
        this.defaultList = defaultList
        return this
    }

    /**
     * Provide default Set implementation. [LinkedHashSet] is used if nothing provided.
     *
     * @param defaultSet - specified Set implementation (as a function from init size)
     * @return the builder with the provided value
     */
    fun setDefaultSet(defaultSet: CollectionProvider<MutableSet<Any?>>): LoadSettingsBuilder {
        this.defaultSet = defaultSet
        return this
    }

    /**
     * Provide default Map implementation. [LinkedHashMap] is used if nothing provided.
     *
     * @param defaultMap - specified Map implementation (as a function from init size)
     * @return the builder with the provided value
     */
    fun setDefaultMap(defaultMap: CollectionProvider<MutableMap<Any?, Any?>>): LoadSettingsBuilder {
        this.defaultMap = defaultMap
        return this
    }

    /**
     * Buffer size for incoming data stream. If the incoming stream is already buffered, then changing
     * the buffer does not improve the performance
     *
     * @param bufferSize - buffer size (in bytes) for input data
     * @return the builder with the provided value
     */
    fun setBufferSize(bufferSize: Int): LoadSettingsBuilder {
        this.bufferSize = bufferSize
        return this
    }

    /**
     * YAML 1.2 does require unique keys. To support the backwards compatibility it is possible to
     * select what should happend when non-unique keys are detected.
     *
     * @param allowDuplicateKeys - if `true` then the non-unique keys in a mapping are allowed (last key
     * wins). `false` by default.
     * @return the builder with the provided value
     */
    fun setAllowDuplicateKeys(allowDuplicateKeys: Boolean): LoadSettingsBuilder {
        this.allowDuplicateKeys = allowDuplicateKeys
        return this
    }

    /**
     * Allow only non-recursive keys for maps and sets. By default, is it not allowed. Even though YAML
     * allows to use anything as a key, it may cause unexpected issues when loading recursive
     * structures.
     *
     * @param allowRecursiveKeys - true to allow recursive structures as keys
     * @return the builder with the provided value
     */
    fun setAllowRecursiveKeys(allowRecursiveKeys: Boolean): LoadSettingsBuilder {
        this.allowRecursiveKeys = allowRecursiveKeys
        return this
    }

    /**
     * Restrict the number of aliases for collection nodes to prevent 'billion laughs attack'. The
     * purpose of this setting is to force SnakeYAML to fail before a lot of CPU and memory resources
     * are allocated for the parser. Aliases for scalar nodes do not count because they do not grow
     * exponentially.
     *
     * @param maxAliasesForCollections - max number of aliases. More than 50 might be very dangerous.
     * Default is 50
     * @return the builder with the provided value
     */
    fun setMaxAliasesForCollections(maxAliasesForCollections: Int): LoadSettingsBuilder {
        this.maxAliasesForCollections = maxAliasesForCollections
        return this
    }

    /**
     * Marks are only used for error messages, but they require a lot of memory. `true` by default.
     *
     * @param useMarks - use false to save resources but use less informative error messages (no line
     * and context)
     * @return the builder with the provided value
     */
    fun setUseMarks(useMarks: Boolean): LoadSettingsBuilder {
        this.useMarks = useMarks
        return this
    }

    /**
     * Manage YAML directive value which defines the version of the YAML specification. This parser
     * supports YAML 1.2, but it can parse most of YAML 1.1 and YAML 1.0
     *
     * This function allows to control the version management. For instance if the document contains
     * old version the parser can be adapted to compensate the problem. Or it can fail to indicate
     * that the incoming version is not supported.
     *
     * @param versionFunction - define the way to manage the YAML version. By default, 1.* versions
     * are accepted and treated as YAML 1.2. Other versions fail to parse ([YamlVersionException]
     * is thrown)
     * @return the builder with the provided value
     */
    fun setVersionFunction(versionFunction: SpecVersionMutator): LoadSettingsBuilder {
        this.versionFunction = versionFunction
        return this
    }

    /**
     * Define EnvConfig to parse EVN format. If not set explicitly the variable substitution is not
     * applied
     *
     * @param envConfig - non-empty configuration to substitute variables
     * @return the builder with the provided value
     * @see [Variable
     * substitution](https://bitbucket.org/snakeyaml/snakeyaml-engine/wiki/Documentation.markdown-header-variable-substitution)
     */
    fun setEnvConfig(envConfig: EnvConfig?): LoadSettingsBuilder {
        this.envConfig = envConfig
        return this
    }

    /**
     * Provide a custom property to be used later
     *
     * @param key - the key
     * @param value - the value behind the key
     * @return the builder with the provided value
     */
    fun setCustomProperty(key: SettingKey, value: Any): LoadSettingsBuilder {
        customProperties[key] = value
        return this
    }

    /**
     * Parse comments to the presentation tree (Node). False by default
     *
     * @param parseComments - use true to parse comments to the presentation tree (Node)
     * @return the builder with the provided value
     */
    fun setParseComments(parseComments: Boolean): LoadSettingsBuilder {
        this.parseComments = parseComments
        return this
    }

    /**
     * The max amount of code points for every input YAML document in the stream. Please be aware that
     * byte limit depends on the encoding.
     *
     * @param codePointLimit - the max allowed size of a single YAML document in a stream
     * @return the builder with the provided value
     */
    fun setCodePointLimit(codePointLimit: Int): LoadSettingsBuilder {
        this.codePointLimit = codePointLimit
        return this
    }

    /**
     * Provide either recommended or custom
     * [schema](https://yaml.org/spec/1.2.2/#chapter-10-recommended-schemas) instead of
     * default * [org.snakeyaml.engine.v2.schema.CoreSchema] These 3 are available
     * [org.snakeyaml.engine.v2.schema.FailsafeSchema],
     * [org.snakeyaml.engine.v2.schema.JsonSchema],
     * [org.snakeyaml.engine.v2.schema.CoreSchema].
     *
     * @param schema to be used for parsing
     * @return the builder with the provided value
     */
    fun setSchema(schema: Schema): LoadSettingsBuilder {
        this.schema = schema
        return this
    }

    /**
     * Build immutable LoadSettings
     *
     * @return immutable LoadSettings
     */
    fun build(): LoadSettings {
        return LoadSettings(
            label = label,
            tagConstructors = tagConstructors,
            defaultList = defaultList,
            defaultSet = defaultSet,
            defaultMap = defaultMap,
            versionFunction = versionFunction,
            bufferSize = bufferSize,
            allowDuplicateKeys = allowDuplicateKeys,
            allowRecursiveKeys = allowRecursiveKeys,
            maxAliasesForCollections = maxAliasesForCollections,
            useMarks = useMarks,
            customProperties = customProperties,
            envConfig = envConfig,
            parseComments = parseComments,
            codePointLimit = codePointLimit,
            schema = schema,
        )
    }
}
