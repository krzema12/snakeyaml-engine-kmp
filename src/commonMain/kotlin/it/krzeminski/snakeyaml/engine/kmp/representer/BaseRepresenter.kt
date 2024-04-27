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
import it.krzeminski.snakeyaml.engine.kmp.internal.IdentityHashCode
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
    private val representedObjects: AnchorNodeMap = AnchorNodeMap()

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
        return representedObjects.getOrElse(identityHashCode(objectToRepresent)) {
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
    protected open fun Map.Entry<*, *>.toNodeTuple(): NodeTuple =
        NodeTuple(representData(key), representData(value))

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
                                || !(valueNode is ScalarNode && valueNode.isPlain)
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

private class AnchorNodeMap : MutableMap<IdentityHashCode, Node> {

    private val contents: MutableMap<IdentityHashCode, AnchorNode> = mutableMapOf()
    private val contentsView: MutableMap<IdentityHashCode, Node>
        get() = contents.mapValuesTo(mutableMapOf()) { (_, v) -> v.realNode }

    override val size: Int get() = contents.size
    override val entries: MutableSet<MutableMap.MutableEntry<IdentityHashCode, Node>> get() = contentsView.entries
    override val keys: MutableSet<IdentityHashCode> get() = contentsView.keys
    override val values: MutableCollection<Node> get() = contentsView.values

    override fun clear() = contents.clear()
    override fun containsKey(key: IdentityHashCode): Boolean = contents.containsKey(key)
    override fun containsValue(value: Node): Boolean = contents.containsValue(value)

    override fun get(key: IdentityHashCode): Node? = contents[key]

    override fun isEmpty(): Boolean = contents.isEmpty()
    override fun put(key: IdentityHashCode, value: Node): Node? =
        contents.put(key, AnchorNode(value))

    override fun putAll(from: Map<out IdentityHashCode, Node>): Unit =
        contents.putAll(from.mapValues { (_, v) -> AnchorNode(v) })

    override fun remove(key: IdentityHashCode): Node? = contents.remove(key)

    @JvmName("getAny")
    operator fun get(key: Any?): Node? = contents[identityHashCode(key)]

    @JvmName("setAny")
    operator fun set(key: Any?, value: Node): Node? =
        contents.put(identityHashCode(key), AnchorNode(value))
}
