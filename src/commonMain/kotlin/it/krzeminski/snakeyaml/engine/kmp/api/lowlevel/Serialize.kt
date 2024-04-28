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
package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.serializer.Serializer

/**
 * Implementation of the step which translates [Node]s to [Event]s
 *
 * @param settings - configuration
 */
class Serialize(
    private val settings: DumpSettings,
) {
    /**
     * Serialize a [Node] and produce events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param node - [Node] to serialize
     * @return serialized events
     */
    fun serializeOne(node: Node): List<Event> = serializeAll(listOf(node))

    /**
     * Serialize [Node]s and produce events.
     *
     * See [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107).
     *
     * @param nodes - [Node]s to serialize
     * @return serialized events
     */
    fun serializeAll(nodes: List<Node>): List<Event> {
        val events = mutableListOf<Event>()
        val serializer = Serializer(settings) { events.add(it) }
        serializer.emitStreamStart()
        for (node in nodes) {
            serializer.serializeDocument(node)
        }
        serializer.emitStreamEnd()
        return events
    }
}
