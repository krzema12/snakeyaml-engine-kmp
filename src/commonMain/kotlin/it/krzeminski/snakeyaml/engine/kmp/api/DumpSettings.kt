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

import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.schema.DEFAULT_SCHEMA
import it.krzeminski.snakeyaml.engine.kmp.schema.Schema
import it.krzeminski.snakeyaml.engine.kmp.serializer.AnchorGenerator
import it.krzeminski.snakeyaml.engine.kmp.serializer.NumberAnchorGenerator

/**
 * Immutable configuration for serialization.
 */
class DumpSettings(
    /**
     * Add '---' at the beginning of the document.
     */
    val isExplicitStart: Boolean = false,

    /**
     * Add '...' at the end of the document.
     */
    val isExplicitEnd: Boolean = false,

    /**
     * Define root [Tag] or let the tag be detected automatically.
     */
    val explicitRootTag: Tag? = null,

    /**
     * Define anchor name generator (by default 'id' + number)
     */
    val anchorGenerator: AnchorGenerator = NumberAnchorGenerator(),

    /**
     * Add YAML [directive](http://yaml.org/spec/1.2/spec.html#id2782090)
     */
    val yamlDirective: SpecVersion? = null,

    /**
     * Add TAG [directive](http://yaml.org/spec/1.2/spec.html#id2782090)
     */
    val tagDirective: Map<String, String> = emptyMap(),

    /**
     * Define flow style
     */
    val defaultFlowStyle: FlowStyle = FlowStyle.AUTO,

    /**
     * Define default scalar style
     */
    val defaultScalarStyle: ScalarStyle = ScalarStyle.PLAIN,

    /**
     * When a String object contains non-printable characters, they are escaped with \\u or \\x
     * notation. Sometimes it is better to transform this data to binary (with the !!binary tag).
     * String objects with printable data are not affected by this setting.
     * Set this to BINARY to force non-printable String to represented as binary (byte array).
     */
    val nonPrintableStyle: NonPrintableStyle = NonPrintableStyle.ESCAPE,

    /**
     * Provide either recommended or custom
     * [schema](https://yaml.org/spec/1.2.2/#chapter-10-recommended-schemas) instead of
     * default [it.krzeminski.snakeyaml.engine.kmp.schema.DEFAULT_SCHEMA]. These 3 are available
     * [it.krzeminski.snakeyaml.engine.kmp.schema.FailsafeSchema],
     * [it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema],
     * [it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema].
     */
    val schema: Schema = DEFAULT_SCHEMA,

    // emitter

    /**
     * Enforce canonical representation
     */
    val isCanonical: Boolean = false,

    /**
     * Use pretty flow style when every value in the flow context gets a separate line.
     */
    val isMultiLineFlow: Boolean = false,

    /**
     * Specify whether to emit non-ASCII printable Unicode characters (emit Unicode char or escape
     * sequence starting with '\\u'). The default value is true. When set to false, printable
     * non-ASCII characters (Cyrillic, Chinese, etc.) will be not printed but escaped (to support ASCII
     * terminals).
     */
    val isUseUnicodeEncoding: Boolean = true,

    /**
     * Define the amount of spaces for the indent in the block flow style. Default is 2.
     */
    val indent: Int = 2,

    /**
     * Adds the specified indent for sequence indicator in the block flow. Default is 0.
     *
     * For better visual results it should be by 2 less than the indent (which is 2 by default).
     * It is 2 chars less because the first char is `-` and the second char is the space after it.
     *
     * Must be in [it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter.VALID_INDENT_RANGE].
     */
    val indicatorIndent: Int = 0,

    /**
     * Set max width for literal scalars. When the scalar representation takes more then the preferred
     * with the scalar will be split into a few lines. The default is 80.
     */
    val width: Int = 80,

    /**
     * If the YAML is created for another platform (for instance, on Windows to be consumed under
     * Linux) than this setting is used to define the line ending. The platform line end is used by
     * default.
     */
    val bestLineBreak: String = "\n",

    /**
     * Define whether to split long lines
     */
    val isSplitLines: Boolean = true,

    /**
     * Define max key length to use simple key (without `?`).
     * [More info](https://yaml.org/spec/1.2/spec.html#id2798057)
     */
    val maxSimpleKeyLength: Int = 128,

    // general

    /**
     * Custom property is the way to give some runtime parameters to be used during dumping
     */
    val customProperties: Map<SettingKey, Any> = emptyMap(),

    /**
     * Set to true to add the indent for sequences to the general indent
     */
    val indentWithIndicator: Boolean = false,

    /**
     * Set to true to add comments from Nodes to the output.
     */
    val dumpComments: Boolean = false,

    /**
     * Disable usage of anchors and aliases while serialising an instance. Recursive objects will not
     * work when they are disabled. (Forces Serializer to skip emitting anchors names, emit Node
     * content instead of Alias, fail with SerializationException if serialized structure is
     * recursive.)
     */
    val isDereferenceAliases: Boolean = false,
) {
    init {
        if (indicatorIndent !in Emitter.VALID_INDICATOR_INDENT_RANGE) {
            throw EmitterException("Indicator indent must be in range ${Emitter.VALID_INDICATOR_INDENT_RANGE}")
        }
        if (maxSimpleKeyLength > 1024) {
            throw YamlEngineException(
                "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057",
            )
        }
        if (indent !in Emitter.VALID_INDENT_RANGE) {
            throw EmitterException("Indent must be at in range ${Emitter.VALID_INDENT_RANGE}")
        }
    }

    fun copy(modifications: DumpSettingsMutable.() -> Unit): DumpSettings {
        val mutable = DumpSettingsMutable(
            isExplicitStart = isExplicitStart,
            isExplicitEnd = isExplicitEnd,
            explicitRootTag = explicitRootTag,
            anchorGenerator = anchorGenerator,
            yamlDirective = yamlDirective,
            tagDirective = tagDirective,
            defaultFlowStyle = defaultFlowStyle,
            defaultScalarStyle = defaultScalarStyle,
            nonPrintableStyle = nonPrintableStyle,
            schema = schema,
            isCanonical = isCanonical,
            isMultiLineFlow = isMultiLineFlow,
            isUseUnicodeEncoding = isUseUnicodeEncoding,
            indent = indent,
            indicatorIndent = indicatorIndent,
            width = width,
            bestLineBreak = bestLineBreak,
            isSplitLines = isSplitLines,
            maxSimpleKeyLength = maxSimpleKeyLength,
            customProperties = customProperties,
            indentWithIndicator = indentWithIndicator,
            dumpComments = dumpComments,
            isDereferenceAliases = isDereferenceAliases,
        )
        mutable.modifications()
        return DumpSettings(
            isExplicitStart = mutable.isExplicitStart,
            isExplicitEnd = mutable.isExplicitEnd,
            explicitRootTag = mutable.explicitRootTag,
            anchorGenerator = mutable.anchorGenerator,
            yamlDirective = mutable.yamlDirective,
            tagDirective = mutable.tagDirective,
            defaultFlowStyle = mutable.defaultFlowStyle,
            defaultScalarStyle = mutable.defaultScalarStyle,
            nonPrintableStyle = mutable.nonPrintableStyle,
            schema = mutable.schema,
            isCanonical = mutable.isCanonical,
            isMultiLineFlow = mutable.isMultiLineFlow,
            isUseUnicodeEncoding = mutable.isUseUnicodeEncoding,
            indent = mutable.indent,
            indicatorIndent = mutable.indicatorIndent,
            width = mutable.width,
            bestLineBreak = mutable.bestLineBreak,
            isSplitLines = mutable.isSplitLines,
            maxSimpleKeyLength = mutable.maxSimpleKeyLength,
            customProperties = mutable.customProperties,
            indentWithIndicator = mutable.indentWithIndicator,
            dumpComments = mutable.dumpComments,
            isDereferenceAliases = mutable.isDereferenceAliases,
        )
    }

    class DumpSettingsMutable(
        var isExplicitStart: Boolean,
        var isExplicitEnd: Boolean,
        var explicitRootTag: Tag?,
        var anchorGenerator: AnchorGenerator,
        var yamlDirective: SpecVersion?,
        var tagDirective: Map<String, String>,
        var defaultFlowStyle: FlowStyle,
        var defaultScalarStyle: ScalarStyle,
        var nonPrintableStyle: NonPrintableStyle,
        var schema: Schema,
        var isCanonical: Boolean,
        var isMultiLineFlow: Boolean,
        var isUseUnicodeEncoding: Boolean,
        var indent: Int,
        var indicatorIndent: Int,
        var width: Int,
        var bestLineBreak: String,
        var isSplitLines: Boolean,
        var maxSimpleKeyLength: Int,
        var customProperties: Map<SettingKey, Any>,
        var indentWithIndicator: Boolean,
        var dumpComments: Boolean,
        var isDereferenceAliases: Boolean,
    )
}
