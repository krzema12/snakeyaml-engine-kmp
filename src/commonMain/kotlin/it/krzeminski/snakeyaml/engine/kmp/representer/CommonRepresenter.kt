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
package it.krzeminski.snakeyaml.engine.kmp.representer

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.RepresentToNode
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

/**
 * Represent standard non-platform specific classes
 * @param settings configuration options
 */
open class CommonRepresenter(
    private val settings: DumpSettings,
) : BaseRepresenter(settings) {
    /** Connect classes to their tags */
    protected val classTags: MutableMap<KClass<*>, Tag> = mutableMapOf()

    /** Create [Node] for [String] */
    private val representString = RepresentToNode { data ->
        val tag: Tag
        var style = ScalarStyle.PLAIN
        var value: String = data.toString()
        if (
            settings.nonPrintableStyle == NonPrintableStyle.BINARY
            && !StreamReader.isPrintable(value)
        ) {
            tag = Tag.BINARY
            val bytes = value.encodeToByteArray()
            // sometimes above will just silently fail - it will return incomplete data
            // it happens when String has invalid code points
            // (for example half surrogate character without other half)
            if (bytes.decodeToString() != value) {
                throw YamlEngineException("invalid string value has occurred")
            }
            @OptIn(ExperimentalEncodingApi::class)
            value = Base64.encode(bytes)
            style = ScalarStyle.LITERAL
        } else {
            tag = Tag.STR
            value = data.toString()
        }
        // if no other scalar style is explicitly set, use literal style for multiline scalars
        if (defaultScalarStyle == ScalarStyle.PLAIN && MULTILINE_PATTERN.containsMatchIn(value)) {
            style = ScalarStyle.LITERAL
        }
        representScalar(tag, value, style)
    }

    /** Create [Node] for [Boolean] */
    protected val representBoolean = RepresentToNode { data ->
        val value = if (data == true) "true" else "false"
        representScalar(Tag.BOOL, value)
    }

    /** Create [Node] for [Byte], [Short], [Int], [Long], [Float], [Double] */
    protected val representNumber = RepresentToNode { data: Any ->
        // For JS, infinity, -infinity and NaN are incorrectly identified as the below types.
        // Because of it, they are represented differently than for other platforms.
        // TODO: fix within https://github.com/krzema12/snakeyaml-engine-kmp/issues/526
        if (
            data is Byte
            || data is Short
            || data is Int
            || data is Long
        ) {
            val value = data.toString()
            representScalar(
                getTag(data::class) { Tag.INT },
                value,
            )
        } else {
            val number = data as Number
            val value = when {
                number is Double && number.isNaN() || number is Float && number.isNaN() -> ".nan"

                number == Double.POSITIVE_INFINITY || number == Float.POSITIVE_INFINITY -> ".inf"

                number == Double.NEGATIVE_INFINITY || number == Float.NEGATIVE_INFINITY -> "-.inf"

                else                                                                    -> number.toString()
            }
            representScalar(
                getTag(data::class) { Tag.FLOAT },
                value,
            )
        }
    }

    /** Create [Node] for [List] */
    private val representList = RepresentToNode { data ->
        representSequence(
            getTag(data::class) { Tag.SEQ },
            data as List<*>,
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for [Iterator] */
    private val representIterator = RepresentToNode { data ->
        val iter = data as Iterator<*>
        representSequence(
            getTag(iter::class) { Tag.SEQ },
            Iterable { iter },
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for `Object[]` */
    private val representArray = RepresentToNode { data: Any ->
        val list = (data as Array<*>).asIterable()
        representSequence(Tag.SEQ, list, settings.defaultFlowStyle)
    }

    /**
     * Represents primitive arrays, such as [ShortArray] and [FloatArray], by converting them using the
     * appropriate autoboxing type.
     */
    private val representPrimitiveArray = RepresentToNode { data ->
        val style = settings.defaultFlowStyle
        val iterableData = when (data) {
            is ByteArray    -> data.asIterable()
            is ShortArray   -> data.asIterable()
            is IntArray     -> data.asIterable()
            is LongArray    -> data.asIterable()
            is FloatArray   -> data.asIterable()
            is DoubleArray  -> data.asIterable()
            is CharArray    -> data.asIterable()
            is BooleanArray -> data.asIterable()
            else            -> throw YamlEngineException("Unexpected primitive '${data::class}'")
        }
        representSequence(Tag.SEQ, iterableData, style)
    }

    /** Create [Node] for [Map] instance */
    private val representMap = RepresentToNode { data ->
        representMapping(
            getTag(data::class) { Tag.MAP },
            data as Map<*, *>,
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for [Set] instances */
    private val representSet = RepresentToNode { data ->
        val set = data as Set<*>
        val value = set.associateWith { null }
        representMapping(
            getTag(data::class) { Tag.SET },
            value,
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for [Enum]s */
    private val representEnum = RepresentToNode { data ->
        representScalar(
            tag = getTag(data::class) { Tag.forType(data::class.simpleName!!) },
            value = (data as Enum<*>).name,
        )
    }

    /** Create [Node] for [ByteArray] */
    private val representByteArray = RepresentToNode { data ->
        representScalar(
            Tag.BINARY,
            @OptIn(ExperimentalEncodingApi::class)
            Base64.encode(data as ByteArray),
            ScalarStyle.LITERAL,
        )
    }

    init {
        representers.putAll(
            mapOf(
                String::class to representString,
                Boolean::class to representBoolean,
                Char::class to representString,
                ByteArray::class to representByteArray,

                // primitive arrays
                ShortArray::class to representPrimitiveArray,
                IntArray::class to representPrimitiveArray,
                LongArray::class to representPrimitiveArray,
                FloatArray::class to representPrimitiveArray,
                DoubleArray::class to representPrimitiveArray,
                CharArray::class to representPrimitiveArray,
                BooleanArray::class to representPrimitiveArray,
            ),
        )

        parentClassRepresenters.putAll(
            mapOf(
                Number::class to representNumber,
                List::class to representList,
                Map::class to representMap,
                Set::class to representSet,
                Iterator::class to representIterator,
                Array::class to representArray,
                Enum::class to representEnum,
            ),
        )
    }

    /**
     * Define the way to get a [Tag] for any class.
     *
     * @param clazz The class to serialise.
     * @param defaultTag The tag to use if there is no explicit configuration.
     * @return the [Tag] for output
     */
    protected inline fun getTag(
        clazz: KClass<*>,
        defaultTag: () -> Tag,
    ): Tag = classTags.getOrElse(clazz, defaultTag)

    companion object {
        /** all chars that represent a new line */
        private val MULTILINE_PATTERN = Regex("[\n\u0085]")
    }
}
