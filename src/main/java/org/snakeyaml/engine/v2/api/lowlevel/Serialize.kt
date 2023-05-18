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
package org.snakeyaml.engine.v2.api.lowlevel

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.emitter.Emitable
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.serializer.Serializer
import java.util.Objects

/**
 * Implementation of the step which translates Nodes to Events
 */
class Serialize(settings: DumpSettings) {
    private val settings: DumpSettings

    /**
     * Create instance with provided [DumpSettings]
     *
     * @param settings - configuration
     */
    init {
        Objects.requireNonNull(settings, "DumpSettings cannot be null")
        this.settings = settings
    }

    /**
     * Serialize a [Node] and produce events.
     *
     * @param node - [Node] to serialize
     * @return serialized events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun serializeOne(node: Node): List<Event> {
        Objects.requireNonNull(node, "Node cannot be null")
        return serializeAll(listOf(node))
    }

    /**
     * Serialize [Node]s and produce events.
     *
     * @param nodes - [Node]s to serialize
     * @return serialized events
     * @see [Processing Overview](http://www.yaml.org/spec/1.2/spec.html.id2762107)
     */
    fun serializeAll(nodes: List<Node>): List<Event> {
        Objects.requireNonNull(nodes, "Nodes cannot be null")
        val emitableEvents = EmitableEvents()
        val serializer = Serializer(settings, emitableEvents)
        serializer.emitStreamStart()
        for (node in nodes) {
            serializer.serializeDocument(node)
        }
        serializer.emitStreamEnd()
        return emitableEvents.getEvents()
    }
}

internal class EmitableEvents : Emitable {
    private val events: MutableList<Event> = ArrayList()
    override fun emit(event: Event) {
        events.add(event)
    }

    fun getEvents(): List<Event> {
        return events
    }
}