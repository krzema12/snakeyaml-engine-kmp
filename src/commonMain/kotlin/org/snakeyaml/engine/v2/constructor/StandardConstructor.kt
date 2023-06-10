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

import org.snakeyaml.engine.internal.getEnvironmentVariable
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
import org.snakeyaml.engine.v2.resolver.BaseScalarResolver

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
     * Flattening is not required because merge was removed from YAML 1.2. Only check duplications.
     *
     * @param node - mapping to check the duplications
     * @returns the flattened [node]
     */
    private fun flattenMapping(node: MappingNode): MappingNode = processDuplicateKeys(node)

    /**
     * detect and process the duplicate key in mapping according to the configured setting
     *
     * @param node - the source
     * @returns the result of de-duplicating the keys in [node]
     */
    private fun processDuplicateKeys(node: MappingNode): MappingNode {
        val groupedByKey = node.value.groupingBy { tuple ->
            constructKey(tuple.keyNode, node.startMark, tuple.keyNode.startMark)
        }

        return if (groupedByKey.eachCount().none { (_, count) -> count > 1 }) {
            // no duplicate keys - no de-duplication necessary
            node
        } else {
            // duplicate keys detected - must remove duplicated tuples and return a new MappingNode
            val deduplicatedValues = groupedByKey.reduce { key, _, tuple ->
                // subsequent tuples replace previous tuples
                if (!settings.allowDuplicateKeys) {
                    throw DuplicateKeyException(
                        contextMark = node.startMark,
                        key = key!!,
                        problemMark = tuple.keyNode.startMark,
                    )
                } else {
                    tuple
                }
            }.values.toList()

            MappingNode(
                tag = node.tag,
                value = deduplicatedValues,
                flowStyle = node.flowStyle,
                resolved = node.isResolved(),
                startMark = node.startMark,
                endMark = node.endMark
            )
        }
    }

    private fun constructKey(
        keyNode: Node,
        contextMark: Mark?,
        problemMark: Mark?,
    ): Any? {
        val key = constructObject(keyNode)
        try {
            key.hashCode() // check circular dependencies
        } catch (e: Exception) {
            throw ConstructorException(
                context = "while constructing a mapping",
                contextMark = contextMark,
                problem = "found unacceptable key $key",
                problemMark = problemMark,
                cause = e,
            )
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

    /** Create [Set] instances */
    inner class ConstructYamlSet : ConstructNode {
        override fun construct(node: Node?): Any {
            require(node is MappingNode)
            return if (node.isRecursive) {
                constructedObjects[node] ?: createEmptySetForNode(node)
            } else {
                constructSet(node)
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

    /** Create [String] instances */
    inner class ConstructYamlStr : ConstructScalar() {
        override fun construct(node: Node?): String = constructScalar(node)
    }

    /** Create the [List] implementation (configured in setting) */
    inner class ConstructYamlSeq : ConstructNode {
        override fun construct(node: Node?): List<*> {
            val seqNode = node as SequenceNode
            return if (node.isRecursive) {
                createEmptyListForNode(seqNode)
            } else {
                constructSequence(seqNode)
            }
        }

        override fun constructRecursive(node: Node, `object`: Any) {
            if (node.isRecursive) {
                constructSequenceStep2(node as SequenceNode, `object` as MutableList<Any?>)
            } else {
                throw YamlEngineException("Unexpected recursive sequence structure. Node: $node")
            }
        }
    }

    /** Create [Map] instance */
    inner class ConstructYamlMap : ConstructNode {
        override fun construct(node: Node?): Map<*, *> {
            val mappingNode = node as MappingNode
            return if (node.isRecursive) {
                createEmptyMapFor(mappingNode)
            } else {
                constructMapping(mappingNode)
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
     * See [Variable substitution](https://bitbucket.org/snakeyaml/snakeyaml/wiki/Variable%20substitution),
     * [Variable substitution](https://docs.docker.com/compose/compose-file/.variable-substitution)
     */
    inner class ConstructEnv : ConstructScalar() {
        override fun construct(node: Node?): Any {
            val scalar = constructScalar(node)
            val config = settings.envConfig
            return if (config != null) {
                val matchResult = BaseScalarResolver.ENV_FORMAT.matchEntire(scalar)
                    ?: error("failed to match scalar")
                val (name, separator, value) = matchResult.destructured
                val env = getEnv(name)
                val overruled = config.getValueFor(name, separator, value, env)
                overruled ?: apply(name, separator, value, env)
            } else {
                scalar
            }
        }

        /**
         * Implement the logic for missing and unset variables
         *
         * @param name        - variable name in the template
         * @param separator   - separator in the template, can be `:-`, `-`, `:?`, `?`
         * @param value       - default value or the error in the template
         * @param environment - the value from environment for the provided variable
         * @return the value to apply in the template
         */
        fun apply(name: String, separator: String?, value: String, environment: String?): String {
            if (!environment.isNullOrEmpty()) {
                return environment
            } else if (separator != null) { // variable is either unset or empty
                // there is a default value or error
                if (separator == "?" && environment == null) {
                    throw MissingEnvironmentVariableException("Missing mandatory variable $name: $value")
                } else if (separator == ":?") {
                    if (environment == null) {
                        throw MissingEnvironmentVariableException("Missing mandatory variable $name: $value")
                    } else if (environment.isEmpty()) {
                        throw MissingEnvironmentVariableException("Empty mandatory variable $name: $value")
                    }
                }
                if (separator.startsWith(":") && environment.isNullOrEmpty()) {
                    return value
                } else if (environment == null) {
                    return value
                }
            }
            return ""
        }

        /**
         * Get value of the environment variable
         *
         * @param key - the name of the variable
         * @return value or `null` if not set
         */
        private fun getEnv(key: String): String? = getEnvironmentVariable(key)
    }
}
