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
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.schema.Schema
import it.krzeminski.snakeyaml.engine.kmp.serializer.AnchorGenerator
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Immutable configuration for serialisation. Description for all the fields can be found in the
 * builder
 */
class DumpSettings internal constructor(
    val isExplicitStart: Boolean,
    val isExplicitEnd: Boolean,
    @JvmField val explicitRootTag: Tag?,
    @JvmField val anchorGenerator: AnchorGenerator,
    @JvmField val yamlDirective: SpecVersion?,
    @JvmField val tagDirective: Map<String, String>,
    @JvmField val defaultFlowStyle: FlowStyle,
    @JvmField val defaultScalarStyle: ScalarStyle,
    @JvmField val nonPrintableStyle: NonPrintableStyle,
    @JvmField val schema: Schema,
    // emitter
    val isCanonical: Boolean,
    val isMultiLineFlow: Boolean,
    val isUseUnicodeEncoding: Boolean,
    @JvmField val indent: Int,
    @JvmField val indicatorIndent: Int,
    @JvmField val width: Int,
    @JvmField val bestLineBreak: String,
    val isSplitLines: Boolean,
    @JvmField val maxSimpleKeyLength: Int,
    // general
    private val customProperties: Map<SettingKey, Any>,
    @JvmField val indentWithIndicator: Boolean,
    @JvmField val dumpComments: Boolean,
    val isDereferenceAliases: Boolean,
) {

    fun getCustomProperty(key: SettingKey): Any? = customProperties[key]

    companion object {
        @JvmStatic
        fun builder(): DumpSettingsBuilder = DumpSettingsBuilder()
    }
}
