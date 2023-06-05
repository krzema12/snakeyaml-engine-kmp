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
package org.snakeyaml.engine.v2.api

import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.representer.Representer
import org.snakeyaml.engine.v2.serializer.Serializer
import kotlin.jvm.JvmOverloads

/**
 * Common way to serialize any Java instance(s). The instance is stateful. Only one of the 'dump'
 * methods may be called, and it may be called only once.
 * @param settings - Configuration options
 * @param representer - The component to translate Java instances to Nodes
 */
class Dump @JvmOverloads constructor(
    private val settings: DumpSettings,
    private val representer: Representer = Representer(settings),
) {
    /**
     * Dump all the instances from the iterator into a stream with every instance in a separate YAML
     * document
     *
     * @param instancesIterator - instances to serialize
     * @param streamDataWriter - destination I/O writer
     */
    fun dumpAll(
        instancesIterator: Iterator<Any?>,
        streamDataWriter: StreamDataWriter,
    ) {
        val serializer = Serializer(settings, Emitter(settings, streamDataWriter))
        serializer.emitStreamStart()
        for (instance in instancesIterator) {
            val node = representer.represent(instance)
            serializer.serializeDocument(node)
        }
        serializer.emitStreamEnd()
    }

    /**
     * Dump a single instance into a YAML document
     *
     * @param yaml - instance to serialize
     * @param streamDataWriter - destination I/O writer
     */
    fun dump(yaml: Any?, streamDataWriter: StreamDataWriter) {
        val iter = iterator { yield(yaml) }
        dumpAll(iter, streamDataWriter)
    }

    /**
     * Dump all the instances from the iterator into a stream with every instance in a separate YAML
     * document
     *
     * @param instancesIterator - instances to serialize
     * @return String representation of the YAML stream
     */
    fun dumpAllToString(instancesIterator: Iterator<Any?>): String {
        val writer = StringStreamDataWriter()
        dumpAll(instancesIterator, writer)
        return writer.toString()
    }

    /**
     * Dump all the instances from the iterator into a stream with every instance in a separate YAML
     * document
     *
     * @param yaml - instance to serialize
     * @return String representation of the YAML stream
     */
    fun dumpToString(yaml: Any?): String {
        val writer = StringStreamDataWriter()
        dump(yaml, writer)
        return writer.toString()
    }

    /**
     * Dump the provided [Node] into a YAML stream.
     *
     * @param node - YAML node to be serialized to YAML document
     * @param streamDataWriter - stream to write to
     */
    fun dumpNode(node: Node, streamDataWriter: StreamDataWriter) {
        val serializer = Serializer(settings, Emitter(settings, streamDataWriter))
        serializer.emitStreamStart()
        serializer.serializeDocument(node)
        serializer.emitStreamEnd()
    }
}
