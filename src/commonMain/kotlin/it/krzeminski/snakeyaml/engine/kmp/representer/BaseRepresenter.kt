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
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.internal.identityHashCode
import it.krzeminski.snakeyaml.engine.kmp.nodes.*
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

/**
 * Represent basic YAML structures: scalar, sequence, mapping.
 */
abstract class BaseRepresenter(
    /** scalar style */
    @JvmField
    protected val defaultScalarStyle: ScalarStyle = ScalarStyle.PLAIN,

    /** flow style for collections */
    @JvmField
    protected val defaultFlowStyle: FlowStyle = FlowStyle.AUTO,
) : Representer {

    constructor(settings: DumpSettings) : this(
        defaultScalarStyle = settings.defaultScalarStyle,
        defaultFlowStyle = settings.defaultFlowStyle,
    )

    /**
     * Keep representers which must match the class exactly
     */
    @JvmField
    protected val representers: MutableMap<KClass<*>, RepresentToNode> = mutableMapOf()

    /**
     * Keep representers which match a parent of the class to be represented
     */
    @JvmField
    protected val parentClassRepresenters: MutableMap<KClass<*>, RepresentToNode> = mutableMapOf()

    /**
     * Keep references of already represented instances.
     *
     * The order is important (map can be also a sequence of key-values)
     */
    private val representedObjects: MutableMap<Any?, Node> = AnchorNodeMap()

    /** in Java `null` is not a type. So we have to keep the `null` representer separately */
    protected open fun nullRepresenter(): Node = representScalar(Tag.NULL, "null")

    /** the current object to be converted to [Node] */
    private var objectToRepresent: Any? = null

    /**
     * Represent the provided Java instance to a [Node]
     *
     * @param data - Java instance to be represented
     * @return The Node to be serialized
     */
    override fun represent(data: Any?): Node {
        val node = representData(data)
        representedObjects.clear()
        objectToRepresent = null
        return node
    }

    /**
     * Find the representer and use it to create the Node from instance
     *
     * @param data - the source
     * @return Node for the provided source
     */
    private fun representData(data: Any?): Node {
        objectToRepresent = data
        // check for identity
        return representedObjects.getOrElse(objectToRepresent) {
            // check for null first
            if (data == null) {
                nullRepresenter()
            } else {
                val representer = findRepresenterFor(data)
                representer.representData(data)
            }
        }
    }

    /**
     * Find the representer which is suitable to represent the internal structure of the provided
     * instance to a Node
     *
     * @param data - the data to be serialized
     * @return [RepresentToNode] to call to create a Node
     * @throws [YamlEngineException] if there is no representer defined for the class of [data]
     */
    private fun findRepresenterFor(data: Any): RepresentToNode {
        // check the same class
        return representers.getOrElse(data::class) {
            // check the parents
            for ((key, value) in parentClassRepresenters) {
                if (key.isInstance(data)) {
                    return value
                }
            }
            throw YamlEngineException("Representer is not defined for class ${data::class.simpleName}")
        }
    }

    /**
     * Create Node for string, using [ScalarStyle.PLAIN] if possible
     *
     * @param tag - the tag for [Node]
     * @param value - the source
     * @param style - the style
     * @return Node for string
     */
    protected fun representScalar(
        tag: Tag,
        value: String,
        style: ScalarStyle = ScalarStyle.PLAIN,
    ): ScalarNode {
        // TODO try putting defaultScalarStyle in default arg
        return ScalarNode(
            tag = tag,
            value = value,
            scalarStyle = if (style == ScalarStyle.PLAIN) defaultScalarStyle else style,
        )
    }

    /**
     * Create Node
     *
     * @param tag - tag to use in Node
     * @param sequence - the source
     * @param flowStyle - the flow style
     * @return the Node from the source iterable
     */
    protected fun representSequence(
        tag: Tag,
        sequence: Iterable<*>,
        flowStyle: FlowStyle,
    ): SequenceNode {
        val size = (sequence as? List<*>)?.size ?: 10 // default for ArrayList
        val value: ArrayList<Node> = ArrayList(size)
        val node = SequenceNode(tag, value, flowStyle)
        representedObjects[objectToRepresent] = node

        sequence.mapTo(value) { item -> representData(item) }

        if (flowStyle == FlowStyle.AUTO) {
            node.flowStyle = when {
                defaultFlowStyle != FlowStyle.AUTO            -> defaultFlowStyle
                value.none { it is ScalarNode && it.isPlain } -> FlowStyle.BLOCK
                else                                          -> FlowStyle.FLOW
            }
        }
        return node
    }

    /**
     * Create a tuple for one key pair
     *
     * @receiver - Map entry
     * @return the tuple where both key and value are converted to Node
     */
    protected open fun Map.Entry<*, *>.toNodeTuple(): NodeTuple = toNodeTuple(this)

    /**
     * Create a tuple for one key pair.
     * This method can be used by implementation to reuse logic from base class
     *
     * @param entry Map entry
     * @return the tuple where both key and value are converted to Node
     */
    @JvmName("toNodeTupleImpl")
    protected fun toNodeTuple(entry: Map.Entry<*, *>): NodeTuple =
        entry.run { NodeTuple(representData(key), representData(value)) }

    /**
     * Create [Node] for the provided [Map]
     *
     * @param tag - the tag for Node
     * @param mapping - the source
     * @param flowStyle - the style of Node
     * @return Node for the source Map
     */
    protected fun representMapping(
        tag: Tag,
        mapping: Map<*, *>,
        flowStyle: FlowStyle,
    ): MappingNode {
        val value = ArrayList<NodeTuple>(mapping.size)
        val node = MappingNode(tag, value, flowStyle)
        representedObjects[objectToRepresent] = node
        mapping.entries.mapTo(value) { it.toNodeTuple() }
        if (flowStyle == FlowStyle.AUTO) {
            node.flowStyle = when (defaultFlowStyle) {
                FlowStyle.AUTO -> {
                    val anyKeyOrValueNotPlain =
                        value.any { (keyNode, valueNode) ->
                            !(keyNode is ScalarNode && keyNode.isPlain)
                                ||
                                !(valueNode is ScalarNode && valueNode.isPlain)
                        }
                    if (anyKeyOrValueNotPlain) {
                        FlowStyle.BLOCK
                    } else {
                        FlowStyle.FLOW
                    }
                }

                else           -> defaultFlowStyle
            }
        }
        return node
    }
}

/**
 * The [IdentityLikeMap] tries to emulate `java.util.IdentityHashMap` from JVM world.
 * However, considering limitations we have in KMP it makes the following assumption:
 * **primitive types and strings does not have identity (meaning "test" and `new String("test")` would have the same "identity")**.
 * This was made due to the following points on JVM:
 * + [5.1.7. Boxing Conversion](https://docs.oracle.com/javase/specs/jls/se23/html/jls-5.html#jls-5.1.7) - primitives withing range -128..127 will have same identity hashcode
 * + Two equal constant strings will have same identity hashcode. Only creating string explicitly using the constructor will result in a different identity in such case
 *
 * This is done because the main goal is to prevent the recursion and primitive types (and strings) cannot cause it.
 */
private open class IdentityLikeMap<T> private constructor(
    private val contents: MutableMap<Any?, T>,
) : MutableMap<Any?, T> by contents {
    constructor() : this(HashMap())

    override fun containsKey(key: Any?): Boolean = contents.containsKey(key.toIdentityKey())

    override fun get(key: Any?): T? = contents[key.toIdentityKey()]

    override fun put(key: Any?, value: T): T? = contents.put(key.toIdentityKey(), value)

    private fun Any?.toIdentityKey(): Any? = when (this) {
        null,
        is Byte,
        is Short,
        is Int,
        is Long,
        is Float,
        is Double,
        is Boolean,
        is Char,
        is String -> this

        else      -> identityHashCode(this)
    }
}

private class AnchorNodeMap : IdentityLikeMap<Node>() {
    override fun put(key: Any?, value: Node): Node? = super.put(key, AnchorNode(value))
}
