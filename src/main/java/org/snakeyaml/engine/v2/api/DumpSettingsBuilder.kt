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

import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.NonPrintableStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.common.SpecVersion
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.exceptions.EmitterException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.schema.JsonSchema
import org.snakeyaml.engine.v2.schema.Schema
import org.snakeyaml.engine.v2.serializer.AnchorGenerator
import org.snakeyaml.engine.v2.serializer.NumberAnchorGenerator
import java.util.Optional

/**
 * Builder pattern implementation for DumpSettings
 */
class DumpSettingsBuilder internal constructor() {
    private var customProperties: MutableMap<SettingKey, Any> = mutableMapOf()
    private var explicitStart = false
    private var explicitEnd = false
    private var nonPrintableStyle: NonPrintableStyle = NonPrintableStyle.ESCAPE
    private var explicitRootTag: Optional<Tag> = Optional.empty()
    private var anchorGenerator: AnchorGenerator = NumberAnchorGenerator()
    private var yamlDirective: Optional<SpecVersion> = Optional.empty()
    private var tagDirective: Map<String, String> = emptyMap()
    private var defaultFlowStyle: FlowStyle = FlowStyle.AUTO
    private var defaultScalarStyle: ScalarStyle = ScalarStyle.PLAIN

    // emitter
    private var canonical = false
    private var multiLineFlow = false
    private var useUnicodeEncoding = true
    private var indent = 2
    private var indicatorIndent = 0
    private var width = 80
    private var bestLineBreak = "\n"
    private var splitLines = true
    private var maxSimpleKeyLength = 128
    private var indentWithIndicator = false
    private var dumpComments = false
    private var schema: Schema = JsonSchema()

    /**
     * Define flow style
     *
     * @param defaultFlowStyle - specify the style
     * @return the builder with the provided value
     */
    fun setDefaultFlowStyle(defaultFlowStyle: FlowStyle): DumpSettingsBuilder {
        this.defaultFlowStyle = defaultFlowStyle
        return this
    }

    /**
     * Define default scalar style
     *
     * @param defaultScalarStyle - specify the scalar style
     * @return the builder with the provided value
     */
    fun setDefaultScalarStyle(defaultScalarStyle: ScalarStyle): DumpSettingsBuilder {
        this.defaultScalarStyle = defaultScalarStyle
        return this
    }

    /**
     * Add '---' in the beginning of the document
     *
     * @param explicitStart - true if the document start must be explicitly indicated
     * @return the builder with the provided value
     */
    fun setExplicitStart(explicitStart: Boolean): DumpSettingsBuilder {
        this.explicitStart = explicitStart
        return this
    }

    /**
     * Define anchor name generator (by default 'id' + number)
     *
     * @param anchorGenerator - specified function to create anchor names
     * @return the builder with the provided value
     */
    fun setAnchorGenerator(anchorGenerator: AnchorGenerator): DumpSettingsBuilder {
        this.anchorGenerator = anchorGenerator
        return this
    }

    /**
     * Define root [Tag] or let the tag to be detected automatically
     *
     * @param explicitRootTag - specify the root tag
     * @return the builder with the provided value
     */
    fun setExplicitRootTag(explicitRootTag: Optional<Tag>): DumpSettingsBuilder {
        this.explicitRootTag = explicitRootTag
        return this
    }

    /**
     * Add '...' in the end of the document
     *
     * @param explicitEnd - true if the document end must be explicitly indicated
     * @return the builder with the provided value
     */
    fun setExplicitEnd(explicitEnd: Boolean): DumpSettingsBuilder {
        this.explicitEnd = explicitEnd
        return this
    }

    /**
     * Add YAML [directive](http://yaml.org/spec/1.2/spec.html#id2782090)
     *
     * @param yamlDirective - the version to be used in the directive
     * @return the builder with the provided value
     */
    fun setYamlDirective(yamlDirective: Optional<SpecVersion>): DumpSettingsBuilder {
        this.yamlDirective = yamlDirective
        return this
    }

    /**
     * Add TAG [directive](http://yaml.org/spec/1.2/spec.html#id2782090)
     *
     * @param tagDirective - the data to create TAG directive
     * @return the builder with the provided value
     */
    fun setTagDirective(tagDirective: Map<String, String>): DumpSettingsBuilder {
        this.tagDirective = tagDirective
        return this
    }

    /**
     * Enforce canonical representation
     *
     * @param canonical - specify if the canonical representation must be used
     * @return the builder with the provided value
     */
    fun setCanonical(canonical: Boolean): DumpSettingsBuilder {
        this.canonical = canonical
        return this
    }

    /**
     * Use pretty flow style when every value in the flow context gets a separate line.
     *
     * @param multiLineFlow - set false to output all values in a single line.
     * @return the builder with the provided value
     */
    fun setMultiLineFlow(multiLineFlow: Boolean): DumpSettingsBuilder {
        this.multiLineFlow = multiLineFlow
        return this
    }

    /**
     * Specify whether to emit non-ASCII printable Unicode characters (emit Unicode char or escape
     * sequence starting with '\\u') The default value is true. When set to false then printable
     * non-ASCII characters (Cyrillic, Chinese etc) will be not printed but escaped (to support ASCII
     * terminals)
     *
     * @param useUnicodeEncoding - true to use Unicode for "Я", false to use "\u0427" for the same
     * char (if useUnicodeEncoding is false then all non-ASCII characters are escaped)
     * @return the builder with the provided value
     */
    fun setUseUnicodeEncoding(useUnicodeEncoding: Boolean): DumpSettingsBuilder {
        this.useUnicodeEncoding = useUnicodeEncoding
        return this
    }

    /**
     * Define the amount of the spaces for the indent in the block flow style. Default is 2.
     *
     * @param indent - the number of spaces. Must be within the range
     * org.snakeyaml.engine.v2.emitter.Emitter.MIN_INDENT and
     * org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT
     * @return the builder with the provided value
     */
    fun setIndent(indent: Int): DumpSettingsBuilder {
        if (indent !in Emitter.VALID_INDENT_RANGE) {
            throw EmitterException("Indent must be at in range ${Emitter.VALID_INDENT_RANGE}")
        }
        this.indent = indent
        return this
    }

    /**
     * Adds the specified indent for sequence indicator in the block flow. Default is 0.
     *
     * For better visual results it should be by 2 less than the indent (which is 2 by default).
     * It is 2 chars less because the first char is `-` and the second char is the space after it.
     *
     * @param indicatorIndent - must be non-negative and less than
     * org.snakeyaml.engine.v2.emitter.Emitter.MAX_INDENT - 1
     * @return the builder with the provided value
     */
    fun setIndicatorIndent(indicatorIndent: Int): DumpSettingsBuilder {
        if (indicatorIndent !in Emitter.VALID_INDICATOR_INDENT_RANGE) {
            throw EmitterException("Indicator indent must be in range ${Emitter.VALID_INDICATOR_INDENT_RANGE}")
        }
        this.indicatorIndent = indicatorIndent
        return this
    }

    /**
     * Set max width for literal scalars. When the scalar representation takes more then the preferred
     * with the scalar will be split into a few lines. The default is 80.
     *
     * @param width - the width
     * @return the builder with the provided value
     */
    fun setWidth(width: Int): DumpSettingsBuilder {
        this.width = width
        return this
    }

    /**
     * If the YAML is created for another platform (for instance on Windows to be consumed under
     * Linux) than this setting is used to define the line ending. The platform line end is used by
     * default.
     *
     * @param bestLineBreak - `\r\n` or `\n`
     * @return the builder with the provided value
     */
    fun setBestLineBreak(bestLineBreak: String): DumpSettingsBuilder {
        this.bestLineBreak = bestLineBreak
        return this
    }

    /**
     * Define whether to split long lines
     *
     * @param splitLines - true to split long lines
     * @return the builder with the provided value
     */
    fun setSplitLines(splitLines: Boolean): DumpSettingsBuilder {
        this.splitLines = splitLines
        return this
    }

    /**
     * Define max key length to use simple key (without `?`).
     * [More info](https://yaml.org/spec/1.2/spec.html#id2798057)
     *
     * @param maxSimpleKeyLength - the limit after which the key gets explicit key indicator '?'
     * @return the builder with the provided value
     */
    fun setMaxSimpleKeyLength(maxSimpleKeyLength: Int): DumpSettingsBuilder {
        if (maxSimpleKeyLength > 1024) {
            throw YamlEngineException(
                "The simple key must not span more than 1024 stream characters. See https://yaml.org/spec/1.2/spec.html#id2798057",
            )
        }
        this.maxSimpleKeyLength = maxSimpleKeyLength
        return this
    }

    /**
     * When String object contains non-printable characters, they are escaped with \\u or \\x
     * notation. Sometimes it is better to transform this data to binary (with the !!binary tag).
     * String objects with printable data are non affected by this setting.
     *
     * @param nonPrintableStyle - set this to BINARY to force non-printable String to represented as
     * binary (byte array)
     * @return the builder with the provided value
     */
    fun setNonPrintableStyle(nonPrintableStyle: NonPrintableStyle): DumpSettingsBuilder {
        this.nonPrintableStyle = nonPrintableStyle
        return this
    }

    /**
     * Custom property is the way to give some runtime parameters to be used during dumping
     *
     * @param key - the key
     * @param value - the value behind the key
     * @return the builder with the provided value
     */
    fun setCustomProperty(key: SettingKey, value: Any): DumpSettingsBuilder {
        customProperties[key] = value
        return this
    }

    /**
     * Set to true to add the indent for sequences to the general indent
     *
     * @param indentWithIndicator - true when indent for sequences is added to general
     * @return the builder with the provided value
     */
    fun setIndentWithIndicator(indentWithIndicator: Boolean): DumpSettingsBuilder {
        this.indentWithIndicator = indentWithIndicator
        return this
    }

    /**
     * Set to true to add comments from Nodes to
     *
     * @param dumpComments - true when comments should be dumped (serialised)
     * @return the builder with the provided value
     */
    fun setDumpComments(dumpComments: Boolean): DumpSettingsBuilder {
        this.dumpComments = dumpComments
        return this
    }

    /**
     * Provide either recommended or custom
     * [schema](https://yaml.org/spec/1.2.2/#chapter-10-recommended-schemas) instead of
     * default [org.snakeyaml.engine.v2.schema.JsonSchema]. These 3 are available
     * [org.snakeyaml.engine.v2.schema.FailsafeSchema],
     * [org.snakeyaml.engine.v2.schema.JsonSchema],
     * [org.snakeyaml.engine.v2.schema.CoreSchema].
     *
     * @param schema - the tag schema
     * @return the builder with the provided value
     */
    fun setSchema(schema: Schema): DumpSettingsBuilder {
        this.schema = schema
        return this
    }

    /**
     * Create immutable DumpSettings
     *
     * @return DumpSettings with the provided values
     */
    fun build(): DumpSettings {
        return DumpSettings(
            isExplicitStart = explicitStart,
            isExplicitEnd = explicitEnd,
            explicitRootTag = explicitRootTag,
            anchorGenerator = anchorGenerator,
            yamlDirective = yamlDirective,
            tagDirective = tagDirective,
            defaultFlowStyle = defaultFlowStyle,
            defaultScalarStyle = defaultScalarStyle,
            nonPrintableStyle = nonPrintableStyle,
            schema = schema,
            // emitter
            isCanonical = canonical,
            isMultiLineFlow = multiLineFlow,
            isUseUnicodeEncoding = useUnicodeEncoding,
            indent = indent,
            indicatorIndent = indicatorIndent,
            width = width,
            bestLineBreak = bestLineBreak,
            isSplitLines = splitLines,
            maxSimpleKeyLength = maxSimpleKeyLength,
            customProperties = customProperties,
            indentWithIndicator = indentWithIndicator,
            dumpComments = dumpComments,
        )
    }
}
