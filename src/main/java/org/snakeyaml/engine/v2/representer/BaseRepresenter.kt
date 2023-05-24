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

import org.snakeyaml.engine.v2.api.RepresentToNode
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.AnchorNode
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeTuple
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.util.IdentityHashMap
import kotlin.reflect.KClass


/**
 * Represent basic YAML structures: scalar, sequence, mapping
 */
abstract class BaseRepresenter(

    /** scalar style */
    @JvmField
    protected val defaultScalarStyle: ScalarStyle = ScalarStyle.PLAIN,

    /** flow style for collections */
    @JvmField
    protected val defaultFlowStyle: FlowStyle = FlowStyle.AUTO,
) {
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
    private val representedObjects: MutableMap<Any?, AnchorNode> = IdentityHashMap()

    /** in Java `null` is not a type. So we have to keep the null representer separately */
    protected abstract val nullRepresenter: RepresentToNode

    /** the current object to be converted to [Node] */
    private var objectToRepresent: Any? = null

    /**
     * Represent the provided Java instance to a Node
     *
     * @param data - Java instance to be represented
     * @return The Node to be serialized
     */
    fun represent(data: Any?): Node {
        val node = representData(data)
        representedObjects.clear()
        objectToRepresent = null
        return node
    }

    /**
     * Find the representer which is suitable to represent the internal structure of the provided
     * instance to a Node
     *
     * @param data - the data to be serialized
     * @return RepresentToNode to call to create a Node
     */
    private fun findRepresenterFor(data: Any): RepresentToNode? {
        val clazz = data::class
        println("finding representer for ${clazz.qualifiedName}")
        // check the same class
        return if (clazz in representers) {
            representers[clazz]
        } else {
            // check the parents
            for ((key, value) in parentClassRepresenters) {
                if (key.isInstance(data)) {
                    return value
                }
            }
            null
        }
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
        return if (representedObjects.containsKey(objectToRepresent)) {
            representedObjects.getValue(objectToRepresent)
        } else {
            // check for null first
            if (data == null) {
                nullRepresenter.representData(Unit)
            } else {
                val representer = findRepresenterFor(data)
                    ?: throw YamlEngineException("Representer is not defined for class ${data::class.java.name}")
                representer.representData(data)
            }
        }
    }

    /**
     * Create Node for string, using PLAIN scalar style if possible
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
    ): Node {
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
    protected fun representSequence(tag: Tag, sequence: Iterable<*>, flowStyle: FlowStyle): Node {
        var size = 10 // default for ArrayList
        if (sequence is List<*>) {
            size = sequence.size
        }
        val value: MutableList<Node> = ArrayList(size)
        val node = SequenceNode(tag, value, flowStyle)
        representedObjects[objectToRepresent] = node
        var bestStyle = FlowStyle.FLOW
        for (item in sequence) {
            val nodeItem = representData(item)
            if (!(nodeItem is ScalarNode && nodeItem.isPlain)) {
                bestStyle = FlowStyle.BLOCK
            }
            value.add(nodeItem)
        }
        if (flowStyle == FlowStyle.AUTO) {
            node.flowStyle = if (defaultFlowStyle != FlowStyle.AUTO) {
                (defaultFlowStyle)
            } else {
                (bestStyle)
            }
        }
        return node
    }

    /**
     * Create a tuple for one key pair
     *
     * @param entry - Map entry
     * @return the tuple where both key and value are converted to Node
     */
    protected open fun representMappingEntry(entry: Map.Entry<*, *>): NodeTuple {
        return NodeTuple(representData(entry.key), representData(entry.value))
    }

    /**
     * Create [Node] for the provided [Map]
     *
     * @param tag - the tag for Node
     * @param mapping - the source
     * @param flowStyle - the style of Node
     * @return Node for the source Map
     */
    protected fun representMapping(tag: Tag, mapping: Map<*, *>, flowStyle: FlowStyle): Node {
        val value: MutableList<NodeTuple> = ArrayList(mapping.size)
        val node = MappingNode(tag, value, flowStyle)
        representedObjects[objectToRepresent] = node
        var bestStyle = FlowStyle.FLOW
        for (entry in mapping.entries) {
            val tuple = representMappingEntry(entry)
            if (!(tuple.keyNode is ScalarNode && tuple.keyNode.isPlain)) {
                bestStyle = FlowStyle.BLOCK
            }
            if (!(tuple.valueNode is ScalarNode && tuple.valueNode.isPlain)) {
                bestStyle = FlowStyle.BLOCK
            }
            value.add(tuple)
        }
        if (flowStyle == FlowStyle.AUTO) {
            node.flowStyle =
                if (defaultFlowStyle != FlowStyle.AUTO) {
                    defaultFlowStyle
                } else {
                    bestStyle
                }
        }
        return node
    }

    companion object {
        private operator fun MutableMap<Any?, AnchorNode>.set(
            key: Any?,
            value: Node,
        ): AnchorNode? = put(key, AnchorNode(value))
    }
}
