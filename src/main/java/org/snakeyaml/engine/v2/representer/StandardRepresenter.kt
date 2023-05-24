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
package org.snakeyaml.engine.v2.representer

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.RepresentToNode
import org.snakeyaml.engine.v2.common.NonPrintableStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Optional
import java.util.UUID
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Represent standard Java classes
 * @param settings - configuration options
 */
open class StandardRepresenter(
    private val settings: DumpSettings,
) : BaseRepresenter(
    settings.defaultScalarStyle,
    settings.defaultFlowStyle,
) {
    /**
     * Connect classes to their tags
     */
    private val classTags: MutableMap<KClass<*>, Tag> = HashMap()

    /** Create `null` [Node] */
    override val nullRepresenter: RepresentToNode = RepresentToNode {
        representScalar(Tag.NULL, "null")
    }

    /** Create [Node] for [Boolean] */
    protected val representBoolean = RepresentToNode { data ->
        val value = if (data == true) "true" else "false"
        representScalar(Tag.BOOL, value)
    }

    /** Create [Node] for [List] */
    private val representList = RepresentToNode { data ->
        representSequence(
            getTag(data::class, Tag.SEQ),
            data as List<*>,
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for [Iterator] */
    private val representIterator = RepresentToNode { data ->
        val iter = data as Iterator<*>
        representSequence(
            getTag(iter::class, Tag.SEQ),
            Iterable { iter },
            settings.defaultFlowStyle,
        )
    }

    /** Create [Node] for [Byte], [Short], [Integer], [Long], [BigInteger] */
    private val representNumber = RepresentToNode { data: Any ->
        val tag: Tag
        val value: String
        if (
            data is Byte
            || data is Short
            || data is Int
            || data is Long
            || data is BigInteger
        ) {
            tag = Tag.INT
            value = data.toString()
        } else {
            val number = data as Number
            tag = Tag.FLOAT
            value = when {
                number is Double && number.isNaN() || number is Float && number.isNaN() -> ".nan"

                number == Double.POSITIVE_INFINITY || number == Float.POSITIVE_INFINITY -> ".inf"

                number == Double.NEGATIVE_INFINITY || number == Float.NEGATIVE_INFINITY -> "-.inf"

                else                                                                    -> number.toString()
            }
        }
        representScalar(getTag(data::class, tag), value)
    }

    /** Create [Node] for `Object[]` */
    private val representArray = RepresentToNode { data: Any ->
        val list = (data as Array<*>).asIterable()
        representSequence(Tag.SEQ, list, settings.defaultFlowStyle)
    }


    /** Create [Node] for [Map] instance */
    private val representMap = RepresentToNode { data ->
        representMapping(
            getTag(data::class, Tag.MAP),
            data as Map<*, *>,
            settings.defaultFlowStyle,
        )
    }

    init {
        representers[String::class] = RepresentString() as RepresentToNode
        representers[Boolean::class] = representBoolean
        representers[Char::class] = RepresentString()
        representers[UUID::class] = RepresentUuid()
        representers[Optional::class] = RepresentOptional()
        representers[ByteArray::class] = RepresentByteArray()

        val primitiveArray = RepresentPrimitiveArray()
        representers[ShortArray::class] = primitiveArray
        representers[IntArray::class] = primitiveArray
        representers[LongArray::class] = primitiveArray
        representers[FloatArray::class] = primitiveArray
        representers[DoubleArray::class] = primitiveArray
        representers[CharArray::class] = primitiveArray
        representers[BooleanArray::class] = primitiveArray

        parentClassRepresenters[Number::class] = representNumber
        parentClassRepresenters[List::class] = representList
        parentClassRepresenters[Map::class] = representMap
        parentClassRepresenters[Set::class] = RepresentSet()
        parentClassRepresenters[Iterator::class] = representIterator
        parentClassRepresenters[Array::class] = representArray
        parentClassRepresenters[Enum::class] = RepresentEnum()
    }

    /**
     * Define the way to get the [Tag] for any class
     *
     * @param clazz      - the class to serialise
     * @param defaultTag - the tag to use if there is no explicit configuration
     * @return the [Tag] for output
     */
    private fun getTag(clazz: KClass<*>, defaultTag: Tag): Tag = classTags.getOrDefault(clazz, defaultTag)

    /** Create [Node] for [String] */
    inner class RepresentString : RepresentToNode {
        override fun representData(data: Any): Node {
            val tag: Tag
            var style = ScalarStyle.PLAIN
            var value: String = data.toString()
            if (settings.nonPrintableStyle == NonPrintableStyle.BINARY
                && !StreamReader.isPrintable(value)
            ) {
                tag = Tag.BINARY
                val bytes = value.toByteArray(StandardCharsets.UTF_8)
                // sometimes above will just silently fail - it will return incomplete data
                // it happens when String has invalid code points
                // (for example half surrogate character without other half)
                val checkValue = String(bytes, StandardCharsets.UTF_8)
                if (checkValue != value) {
                    throw YamlEngineException("invalid string value has occurred")
                }
                value = Base64.getEncoder().encodeToString(bytes)
                style = ScalarStyle.LITERAL
            } else {
                tag = Tag.STR
                value = data.toString()
            }
            // if no other scalar style is explicitly set, use literal style for
            // multiline scalars
            if (defaultScalarStyle == ScalarStyle.PLAIN && MULTILINE_PATTERN.matcher(value).find()) {
                style = ScalarStyle.LITERAL
            }
            return representScalar(tag, value, style)
        }
    }


    /**
     * Represents primitive arrays, such as `short[]` and `float[]`, by converting them using the
     * appropriate autoboxing type.
     */
    inner class RepresentPrimitiveArray : RepresentToNode {
        override fun representData(data: Any): Node {
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
                else            -> throw YamlEngineException("Unexpected primitive '${data::class.java.componentType.canonicalName}'")
            }
            return representSequence(Tag.SEQ, iterableData, style)
        }
    }

    /**
     * Create [Node] for [Set] instances
     */
    inner class RepresentSet : RepresentToNode {
        override fun representData(data: Any): Node {
            val value: MutableMap<Any?, Any?> = LinkedHashMap()
            val set = data as Set<*>
            for (key in set) {
                value[key] = null
            }
            return representMapping(
                getTag(data::class, Tag.SET),
                value,
                settings.defaultFlowStyle,
            )
        }
    }

    /** Create [Node] for [Enum]s */
    inner class RepresentEnum : RepresentToNode {
        override fun representData(data: Any): Node {
            val tag = Tag(data::class)
            return representScalar(getTag(data::class, tag), (data as Enum<*>).name)
        }
    }

    /** Create [Node] for [ByteArray] */
    inner class RepresentByteArray : RepresentToNode {
        override fun representData(data: Any): Node {
            return representScalar(
                Tag.BINARY,
                Base64.getEncoder().encodeToString(data as ByteArray),
                ScalarStyle.LITERAL,
            )
        }
    }

    /**
     * Create Node for UUID
     */
    inner class RepresentUuid : RepresentToNode {
        override fun representData(data: Any): Node {
            return representScalar(
                getTag(
                    data::class,
                    Tag(UUID::class),
                ),
                data.toString(),
            )
        }
    }

    /**
     * Create Node for [Optional] instance (the value of null)
     */
    inner class RepresentOptional : RepresentToNode {
        override fun representData(data: Any): Node {
            val opt = data as Optional<*>
            return if (opt.isPresent) {
                val node = represent(opt.get())
                node.tag = Tag(Optional::class)
                node
            } else {
                representScalar(Tag.NULL, "null")
            }
        }
    }

    companion object {
        /**
         * all chars that represent a new line
         */
        private val MULTILINE_PATTERN: Pattern = Pattern.compile("[\n\u0085\u2028\u2029]")
    }
}
