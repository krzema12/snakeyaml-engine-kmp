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
package org.snakeyaml.engine.v2.constructor

import org.snakeyaml.engine.v2.api.ConstructNode
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.exceptions.ConstructorException
import org.snakeyaml.engine.v2.exceptions.DuplicateKeyException
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.exceptions.MissingEnvironmentVariableException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver
import java.util.Optional
import java.util.TreeSet

/**
 * Construct standard Java classes
 */
open class StandardConstructor(settings: LoadSettings) : BaseConstructor(settings) {
    init {
        tagConstructors[Tag.SET] = ConstructYamlSet()
        tagConstructors[Tag.STR] = ConstructYamlStr()
        tagConstructors[Tag.SEQ] = ConstructYamlSeq()
        tagConstructors[Tag.MAP] = ConstructYamlMap()
        tagConstructors[Tag.ENV_TAG] = ConstructEnv()

        // apply the tag constructors from the provided schema
        tagConstructors.putAll(settings.schema.schemaTagConstructors)

        // the explicit config overrides all
        tagConstructors.putAll(settings.tagConstructors)
    }

    /**
     * Flattening is not required because merge was removed from YAML 1.2 Only check duplications
     *
     * @param node - mapping to check the duplications
     */
    protected fun flattenMapping(node: MappingNode) {
        processDuplicateKeys(node)
    }

    /**
     * detect and process the duplicate key in mapping according to the configured setting
     *
     * @param node - the source
     */
    protected fun processDuplicateKeys(node: MappingNode) {
        val nodeValue = node.value
        val keys: MutableMap<Any?, Int> = HashMap(nodeValue.size)
        val toRemove = TreeSet<Int>()
        var i = 0
        for (tuple in nodeValue) {
            val keyNode = tuple.keyNode
            val key = constructKey(keyNode, node.startMark, tuple.keyNode.startMark)
            val prevIndex = keys.put(key, i)
            if (prevIndex != null) {
                if (!settings.allowDuplicateKeys) {
                    throw DuplicateKeyException(
                        node.startMark, key!!,
                        tuple.keyNode.startMark,
                    )
                }
                toRemove.add(prevIndex)
            }
            i += 1
        }
        val indices2remove = toRemove.descendingIterator()
        while (indices2remove.hasNext()) {
            nodeValue.removeAt(indices2remove.next())
        }
    }

    private fun constructKey(
        keyNode: Node, contextMark: Optional<Mark>,
        problemMark: Optional<Mark>,
    ): Any? {
        val key = constructObject(keyNode)
        if (key != null) {
            try {
                key.hashCode() // check circular dependencies
            } catch (e: Exception) {
                throw ConstructorException(
                    "while constructing a mapping", contextMark,
                    "found unacceptable key $key", problemMark, e,
                )
            }
        }
        return key
    }

    override fun constructMapping2ndStep(node: MappingNode, mapping: MutableMap<Any?, Any?>) {
        flattenMapping(node)
        super.constructMapping2ndStep(node, mapping)
    }

    override fun constructSet2ndStep(node: MappingNode, set: MutableSet<Any?>) {
        flattenMapping(node)
        super.constructSet2ndStep(node, set)
    }

    /**
     * Create Set instances
     */
    inner class ConstructYamlSet : ConstructNode {
        override fun construct(node: Node?): Any? {
            return if (node!!.isRecursive) {
                if (constructedObjects.containsKey(node)) constructedObjects[node] else createEmptySetForNode(
                    (node as MappingNode?)!!,
                )
            } else {
                constructSet((node as MappingNode?)!!)
            }
        }

        override fun constructRecursive(node: Node, `object`: Any) {
            if (node.isRecursive) {
                constructSet2ndStep(node as MappingNode, `object` as MutableSet<Any?>)
            } else {
                throw YamlEngineException("Unexpected recursive set structure. Node: $node")
            }
        }
    }

    /**
     * Create String instances
     */
    inner class ConstructYamlStr : ConstructScalar() {
        override fun construct(node: Node?): Any {
            return constructScalar(node)
        }
    }

    /**
     * Create the List implementation (configured in setting)
     */
    inner class ConstructYamlSeq : ConstructNode {
        override fun construct(node: Node?): Any {
            val seqNode = node as SequenceNode?
            return if (node!!.isRecursive) {
                createEmptyListForNode(seqNode!!)
            } else {
                constructSequence(seqNode!!)
            }
        }

        override fun constructRecursive(node: Node, `object`: Any) {
            if (node.isRecursive) {
                constructSequenceStep2((node as SequenceNode), `object` as MutableList<Any?>)
            } else {
                throw YamlEngineException("Unexpected recursive sequence structure. Node: $node")
            }
        }
    }

    /**
     * Create Map instance
     */
    inner class ConstructYamlMap : ConstructNode {
        override fun construct(node: Node?): Any? {
            val mappingNode = node as MappingNode?
            return if (node!!.isRecursive) {
                createEmptyMapFor(mappingNode!!)
            } else {
                constructMapping(mappingNode!!)
            }
        }

        override fun constructRecursive(node: Node, `object`: Any) {
            if (node.isRecursive) {
                constructMapping2ndStep(node as MappingNode, `object` as MutableMap<Any?, Any?>)
            } else {
                throw YamlEngineException("Unexpected recursive mapping structure. Node: $node")
            }
        }
    }

    /**
     * Construct scalar for format `${VARIABLE}` replacing the template with the value from environment.
     *
     * @see [Variable
     * substitution](https://bitbucket.org/snakeyaml/snakeyaml/wiki/Variable%20substitution)
     *
     * @see [Variable
     * substitution](https://docs.docker.com/compose/compose-file/.variable-substitution)
     */
    inner class ConstructEnv : ConstructScalar() {
        override fun construct(node: Node?): Any? {
            val scalar = constructScalar(node)
            val opt = settings.envConfig
            return if (opt.isPresent) {
                val config = opt.get()
                val matcher = JsonScalarResolver.ENV_FORMAT.matcher(scalar)
                matcher.matches()
                val name = matcher.group(1)
                val value = matcher.group(3)
                val nonNullValue = value ?: ""
                val separator = matcher.group(2)
                val env = getEnv(name)
                val overruled = config.getValueFor(name, separator, nonNullValue, env)
                overruled.orElseGet {
                    apply(
                        name,
                        separator,
                        nonNullValue,
                        env,
                    )
                }
            } else {
                scalar
            }
        }

        /**
         * Implement the logic for missing and unset variables
         *
         * @param name        - variable name in the template
         * @param separator   - separator in the template, can be :-, -, :?, ?
         * @param value       - default value or the error in the template
         * @param environment - the value from environment for the provided variable
         * @return the value to apply in the template
         */
        fun apply(name: String, separator: String?, value: String, environment: String?): String {
            if (!environment.isNullOrEmpty()) {
                return environment
            }
            // variable is either unset or empty
            if (separator != null) {
                // there is a default value or error
                if (separator == "?") {
                    if (environment == null) {
                        throw MissingEnvironmentVariableException(
                            "Missing mandatory variable $name: $value",
                        )
                    }
                }
                if (separator == ":?") {
                    if (environment == null) {
                        throw MissingEnvironmentVariableException(
                            "Missing mandatory variable $name: $value",
                        )
                    }
                    if (environment.isEmpty()) {
                        throw MissingEnvironmentVariableException(
                            "Empty mandatory variable $name: $value",
                        )
                    }
                }
                if (separator.startsWith(":")) {
                    if (environment.isNullOrEmpty()) {
                        return value
                    }
                } else {
                    if (environment == null) {
                        return value
                    }
                }
            }
            return ""
        }

        /**
         * Get value of the environment variable
         *
         * @param key - the name of the variable
         * @return value or null if not set
         */
        private fun getEnv(key: String?): String? = System.getenv(key)
    }
}
