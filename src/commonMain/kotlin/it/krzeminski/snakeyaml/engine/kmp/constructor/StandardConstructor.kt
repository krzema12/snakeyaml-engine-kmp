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
import it.krzeminski.snakeyaml.engine.kmp.exceptions.*
import it.krzeminski.snakeyaml.engine.kmp.internal.getEnvironmentVariable
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.resolver.BaseScalarResolver

/**
 * Construct standard Java classes.
 */
open class StandardConstructor(settings: LoadSettings) : BaseConstructor(settings) {

    override val tagConstructors: Map<Tag, ConstructNode> =
        buildMap {
            put(Tag.SET, ConstructYamlSet())
            put(Tag.STR, ConstructYamlStr())
            put(Tag.SEQ, ConstructYamlSeq())
            put(Tag.MAP, ConstructYamlMap())
            put(Tag.ENV_TAG, ConstructEnv())

            // apply the tag constructors from the provided schema
            putAll(settings.schema.schemaTagConstructors)

            // the explicit config overrides all
            putAll(settings.tagConstructors)
        }

    override fun constructMapping2ndStep(node: MappingNode, mapping: MutableMap<Any?, Any?>) {
        validateDuplicateKeys(node)
        super.constructMapping2ndStep(node, mapping)
    }

    override fun constructSet2ndStep(node: MappingNode, set: MutableSet<Any?>) {
        validateDuplicateKeys(node)
        super.constructSet2ndStep(node, set)
    }

    /**
     * detect and process the duplicate key in mapping according to the configured setting
     *
     * @param node the source
     * @throws DuplicateKeyException if duplicated keys were detected and [LoadSettings.allowDuplicateKeys] is `false`.
     */
    private fun validateDuplicateKeys(node: MappingNode) {
        if (settings.allowDuplicateKeys) return

        val groupedByKey = node.value.groupingBy { (keyNode, _) ->
            constructKey(keyNode, node.startMark, keyNode.startMark)
        }

        groupedByKey.reduce { key, _, tuple ->
            // this merge lambda is only called if there are multiple tuples per key
            throw DuplicateKeyException(
                contextMark = node.startMark,
                key = key!!,
                problemMark = tuple.keyNode.startMark,
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
                @Suppress("UNCHECKED_CAST")
                val set = `object` as MutableSet<Any?>
                constructSet2ndStep(node as MappingNode, set)
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
                @Suppress("UNCHECKED_CAST")
                val list = `object` as MutableList<Any?>
                constructSequenceStep2(node as SequenceNode, list)
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
                @Suppress("UNCHECKED_CAST")
                val map =  `object` as MutableMap<Any?, Any?>
                constructMapping2ndStep(node as MappingNode, map)
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
                val env = getEnvironmentVariable(name)
                val overruled = config.getValueFor(name, separator, value, env)
                overruled ?: apply(name, separator, value, env)
            } else {
                scalar
            }
        }

        /**
         * Implement the logic for missing and unset variables
         *
         * @param name         variable name in the template
         * @param separator    separator in the template, can be `:-`, `-`, `:?`, `?`
         * @param value        default value or the error in the template
         * @param environment  the value from environment for the provided variable
         * @return the value to apply in the template
         */
        private fun apply(
            name: String,
            separator: String?,
            value: String,
            environment: String?,
        ): String {
            if (!environment.isNullOrEmpty()) return environment // variable is either unset or empty
            if (separator == null) return ""

            return when (separator) {
                ":-" -> when {
                    environment.isNullOrEmpty() -> value
                    else                        -> ""
                }

                ":?" -> when {
                    environment == null   ->
                        throw MissingEnvironmentVariableException.forMissingVariable(name, value)

                    environment.isEmpty() ->
                        throw MissingEnvironmentVariableException.forEmptyVariable(name, value)

                    else                  ->
                        value
                }

                "-"  -> when (environment) {
                    null -> value
                    else -> ""
                }

                "?"  -> when (environment) {
                    null -> throw MissingEnvironmentVariableException.forMissingVariable(name, value)
                    else -> ""
                }

                else -> ""
            }
        }
    }
}
