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
package it.krzeminski.snakeyaml.engine.kmp.constructor

import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ConstructorException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.*
import kotlin.jvm.JvmField

/**
 * Base code
 */
abstract class BaseConstructor(
    @JvmField
    protected val settings: LoadSettings,
) {
    /**
     * Maps (explicit or implicit) tags to the [ConstructNode] implementation.
     */
    protected abstract val tagConstructors: Map<Tag, ConstructNode>

    @JvmField
    protected val constructedObjects: MutableMap<Node, Any?> = mutableMapOf()

    private val recursiveObjects: MutableSet<Node> = mutableSetOf()

    private val maps2fill: MutableList<RecursiveTuple<MutableMap<Any?, Any?>, RecursiveTuple<Any?, Any?>>> =
        mutableListOf()

    private val sets2fill: MutableList<RecursiveTuple<MutableSet<Any?>, Any?>> = mutableListOf()

    /**
     * Ensure that the stream contains a single document and construct it
     *
     * @param optionalNode - composed Node
     * @return constructed instance
     */
    fun constructSingleDocument(optionalNode: Node?): Any? {
        return if (optionalNode != null && Tag.NULL != optionalNode.tag) {
            construct(optionalNode)
        } else {
            val construct = tagConstructors[Tag.NULL]
                ?: error("missing NULL constructor in tagConstructors $tagConstructors")
            construct.construct(optionalNode)
        }
    }

    /**
     * Construct complete YAML document. Call the second step in case of recursive structures. At the
     * end cleans all the state.
     *
     * @param node root Node
     * @return Java instance
     */
    fun construct(node: Node): Any? {
        return try {
            val data = constructObject(node)
            fillRecursive()
            data
        } catch (e: YamlEngineException) {
            throw e
        } catch (e: RuntimeException) {
            throw YamlEngineException(e)
        } finally {
            constructedObjects.clear()
            recursiveObjects.clear()
        }
    }

    private fun fillRecursive() {
        if (maps2fill.isNotEmpty()) {
            for (entry in maps2fill) {
                val (value1, value2) = entry.value2
                entry.value1[value1] = value2
            }
            maps2fill.clear()
        }
        if (sets2fill.isNotEmpty()) {
            for (value in sets2fill) {
                value.value1.add(value.value2)
            }
            sets2fill.clear()
        }
    }

    /**
     * Construct object from the specified [Node]. Return existing instance if [node] is already
     * constructed.
     *
     * @param node Node to be constructed
     * @return Java instance
     */
    protected fun constructObject(node: Node): Any? {
        return constructedObjects[node] ?: constructObjectNoCheck(node)
    }

    /**
     * Construct object from the specified [Node]. It does not check if existing instance of [node] is already
     * constructed.
     *
     * @param node the source
     * @return instantiated object
     */
    private fun constructObjectNoCheck(node: Node): Any? {
        if (recursiveObjects.contains(node)) {
            throw ConstructorException(
                context = null,
                contextMark = null,
                problem = "found unconstructable recursive node",
                problemMark = node.startMark,
            )
        }
        recursiveObjects.add(node)
        val constructor = findConstructorFor(node)
            ?: throw ConstructorException(
                context = null,
                contextMark = null,
                problem = "could not determine a constructor for the tag " + node.tag,
                problemMark = node.startMark,
            )
        val data = constructedObjects[node] ?: constructor.construct(node)
        constructedObjects[node] = data
        recursiveObjects.remove(node)
        if (node.isRecursive) {
            constructor.constructRecursive(node, data!!)
        }
        return data
    }

    /**
     * Select [ConstructNode] inside the provided [Node] or the one associated with the
     * [Tag]
     *
     * @param node [Node] to construct an instance from
     * @return [ConstructNode] implementation for the specified node
     */
    protected open fun findConstructorFor(node: Node): ConstructNode? {
        val tag = node.tag
        return settings.tagConstructors[tag] ?: tagConstructors[tag]
    }

    /**
     * Create String from the provided scalar node
     *
     * @param node the source
     * @return value of the scalar node
     */
    protected fun constructScalar(node: ScalarNode): String = node.value

    //region DEFAULTS
    /**
     * Create List implementation. By default, it returns the value configured in
     * [LoadSettings.defaultList]. Any custom [List] implementation can be provided.
     *
     * @param node the node to fill the List
     * @return empty List to fill
     */
    protected fun createEmptyListForNode(node: SequenceNode): List<Any?> {
        return settings.defaultList(node.value.size)
    }

    /**
     * Create Set implementation. By default, it returns the value configured in
     * [LoadSettings.defaultSet]. Any custom [Set] implementation can be provided.
     *
     * @param node the node to fill the Set
     * @return empty Set to fill
     */
    protected fun createEmptySetForNode(node: MappingNode): Set<Any?> {
        return settings.defaultSet(node.value.size)
    }

    /**
     * Create Map implementation. By default, it returns the value configured in
     * [LoadSettings.defaultMap]. Any custom [Map] implementation can be provided.
     *
     * @param node the node to fill the [Map]
     * @return empty [Map] to fill
     */
    protected fun createEmptyMapFor(node: MappingNode): Map<Any?, Any?> {
        return settings.defaultMap(node.value.size)
    }
    //endregion

    /**
     * Create instance of [List]
     *
     * @param node the source
     * @return filled [List]
     */
    protected fun constructSequence(node: SequenceNode): List<Any?> {
        val result = settings.defaultList(node.value.size)
        constructSequenceStep2(node, result)
        return result
    }

    /**
     * Fill the collection with the data from provided node
     *
     * @param node the source
     * @param collection the collection to fill
     */
    protected fun constructSequenceStep2(node: SequenceNode, collection: MutableCollection<Any?>) {
        for (child in node.value) {
            collection.add(constructObject(child))
        }
    }

    /**
     * Create instance of [Set] from mapping node
     *
     * @param node the source
     * @return filled [Set]
     */
    protected fun constructSet(node: MappingNode): Set<Any?> {
        val set = settings.defaultSet(node.value.size)
        constructSet2ndStep(node, set)
        return set
    }

    /**
     * Create filled [Map] from the provided Node
     *
     * @param node the source
     * @return filled [Map]
     */
    protected fun constructMapping(node: MappingNode): Map<Any?, Any?> {
        val mapping = settings.defaultMap(node.value.size)
        constructMapping2ndStep(node, mapping)
        return mapping
    }

    /**
     * Fill the mapping with the data from provided node
     *
     * @param node the source
     * @param mapping empty map to be filled
     */
    protected open fun constructMapping2ndStep(node: MappingNode, mapping: MutableMap<Any?, Any?>) {
        val nodeValue = node.value
        for (tuple in nodeValue) {
            val keyNode = tuple.keyNode
            val valueNode = tuple.valueNode
            val key = constructObject(keyNode)
            if (key != null) {
                try {
                    key.hashCode() // check circular dependencies
                } catch (e: Exception) {
                    throw ConstructorException(
                        context = "while constructing a mapping",
                        contextMark = node.startMark,
                        problem = "found unacceptable key $key",
                        problemMark = tuple.keyNode.startMark,
                        cause = e,
                    )
                }
            }
            val value = constructObject(valueNode)
            if (keyNode.isRecursive) {
                if (settings.allowRecursiveKeys) {
                    postponeMapFilling(mapping, key, value)
                } else {
                    throw YamlEngineException(
                        "Recursive key for mapping is detected but it is not configured to be allowed.",
                    )
                }
            } else {
                mapping[key] = value
            }
        }
    }

    /**
     * if keyObject is created it 2 steps we should postpone putting it in map because it may have
     * different hash after initialization compared to clean just created one. And map of course does
     * not observe key hashCode changes.
     *
     * @param mapping the mapping to add key/value
     * @param key the key to add to map
     * @param value the value behind the key
     */
    private fun postponeMapFilling(mapping: MutableMap<Any?, Any?>, key: Any?, value: Any?) {
        maps2fill.add(0, RecursiveTuple(mapping, RecursiveTuple(key, value)))
    }

    /**
     * Fill the Map with the data from the node
     *
     * @param node the source
     * @param set - empty set to fill
     */
    protected open fun constructSet2ndStep(node: MappingNode, set: MutableSet<Any?>) {
        val nodeValue = node.value
        for (tuple in nodeValue) {
            val keyNode = tuple.keyNode
            val key = constructObject(keyNode)
            if (key != null) {
                try {
                    key.hashCode() // check circular dependencies
                } catch (e: Exception) {
                    throw ConstructorException(
                        context = "while constructing a Set",
                        contextMark = node.startMark,
                        problem = "found unacceptable key $key",
                        problemMark = tuple.keyNode.startMark,
                        cause = e,
                    )
                }
            }
            if (keyNode.isRecursive) {
                if (settings.allowRecursiveKeys) {
                    postponeSetFilling(set, key)
                } else {
                    throw YamlEngineException(
                        "Recursive key for mapping is detected but it is not configured to be allowed.",
                    )
                }
            } else {
                set.add(key)
            }
        }
    }

    /**
     * if keyObject is created it 2 steps we should postpone putting it into the set because it may
     * have different hash after initialization compared to clean just created one. And set of course
     * does not observe value hashCode changes.
     *
     * @param set the set to add the key
     * @param key the item to add to the set
     */
    private fun postponeSetFilling(set: MutableSet<Any?>, key: Any?) {
        sets2fill.add(0, RecursiveTuple(set, key))
    }

    private data class RecursiveTuple<T, K>(
        val value1: T,
        val value2: K,
    )
}
