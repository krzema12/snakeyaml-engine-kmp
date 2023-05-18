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
import org.snakeyaml.engine.v2.representer.BaseRepresenter
import org.snakeyaml.engine.v2.representer.StandardRepresenter
import org.snakeyaml.engine.v2.serializer.Serializer
import java.io.StringWriter
import java.util.Objects

/**
 * Common way to serialize any Java instance(s). The instance is stateful. Only one of the 'dump'
 * methods may be called, and it may be called only once.
 */
class Dump @JvmOverloads constructor(
    settings: DumpSettings,
    representer: BaseRepresenter = StandardRepresenter(settings)
) {
    /**
     * Configuration options
     */
    protected var settings: DumpSettings

    /**
     * The component to translate Java instances to Nodes
     */
    protected var representer: BaseRepresenter
    /**
     * Create instance
     *
     * @param settings - configuration
     * @param representer - custom representer
     */
    /**
     * Create instance
     *
     * @param settings - configuration
     */
    init {
        Objects.requireNonNull(settings, "DumpSettings cannot be null")
        Objects.requireNonNull(representer, "Representer cannot be null")
        this.settings = settings
        this.representer = representer
    }

    /**
     * Dump all the instances from the iterator into a stream with every instance in a separate YAML
     * document
     *
     * @param instancesIterator - instances to serialize
     * @param streamDataWriter - destination I/O writer
     */
    fun dumpAll(
        instancesIterator: Iterator<Any?>,
        streamDataWriter: StreamDataWriter
    ) {
        Objects.requireNonNull(instancesIterator, "Iterator cannot be null")
        Objects.requireNonNull(streamDataWriter, "StreamDataWriter cannot be null")
        val serializer = Serializer(settings, Emitter(settings, streamDataWriter))
        serializer.emitStreamStart()
        while (instancesIterator.hasNext()) {
            val instance = instancesIterator.next()
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
    fun dump(yaml: Any, streamDataWriter: StreamDataWriter) {
        val iter = setOf(yaml).iterator()
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
        val writer = StreamToStringWriter()
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
    fun dumpToString(yaml: Any): String {
        val writer = StreamToStringWriter()
        dump(yaml, writer)
        return writer.toString()
    }

    /**
     * Dump the provided Node into a YAML stream.
     *
     * @param node - YAML node to be serialized to YAML document
     * @param streamDataWriter - stream to write to
     */
    fun dumpNode(node: Node, streamDataWriter: StreamDataWriter) {
        Objects.requireNonNull(node, "Node cannot be null")
        Objects.requireNonNull(streamDataWriter, "StreamDataWriter cannot be null")
        val serializer = Serializer(settings, Emitter(settings, streamDataWriter))
        serializer.emitStreamStart()
        serializer.serializeDocument(node)
        serializer.emitStreamEnd()
    }
}

/**
 * Internal helper class to support dumping to String
 */
internal class StreamToStringWriter : StringWriter(), StreamDataWriter {
    override fun flush() {
        TODO("Not yet sure what should be here")
    }
}