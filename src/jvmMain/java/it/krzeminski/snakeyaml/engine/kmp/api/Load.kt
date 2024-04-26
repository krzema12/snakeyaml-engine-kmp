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
package it.krzeminski.snakeyaml.engine.kmp.api

import okio.source
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import it.krzeminski.snakeyaml.engine.kmp.constructor.StandardConstructor
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import java.io.InputStream
import java.io.Reader

/**
 * Common way to load Java instance(s). This class is not thread-safe. Which means that all the
 * methods of the same instance can be called only by one thread. It is better to create an instance
 * for every YAML stream. The instance is stateful. Only one of the 'load' methods may be called,
 * and it may be called only once.
 *
 * @param settings - configuration
 * @param constructor - custom YAML constructor
 */
class Load @JvmOverloads constructor(
    private val settings: LoadSettings,
    private val constructor: BaseConstructor = StandardConstructor(settings),
) {

    /**
     * Create Composer
     *
     * @param streamReader - the input
     * @return configured Composer
     */
    private fun createComposer(streamReader: StreamReader): Composer =
        Composer(settings, ParserImpl(settings, streamReader))

    /**
     * Create Composer
     *
     * @param yamlStream - the input
     * @return configured Composer
     */
    private fun createComposer(yamlStream: InputStream): Composer =
        createComposer(StreamReader(settings, YamlUnicodeReader(yamlStream.source())))

    /**
     * Create Composer
     *
     * @param yaml - the input
     * @return configured Composer
     */
    private fun createComposer(yaml: String): Composer =
        createComposer(StreamReader(settings, yaml))

    /**
     * Create Composer
     *
     * @param yamlReader - the input
     * @return configured Composer
     */
    private fun createComposer(yamlReader: Reader): Composer =
        createComposer(StreamReader(settings, yamlReader.readText()))

    /**
     * Load a single document with the provided [composer]
     *
     * @param composer - the component to create the Node
     * @return deserialised YAML document
     */
    private fun loadOne(composer: Composer): Any? {
        val nodeOptional = composer.getSingleNode()
        return constructor.constructSingleDocument(nodeOptional)
    }

    /**
     * Parse the only YAML document in a stream and produce the corresponding Java object.
     *
     * @param yamlStream - data to load from (BOM is respected to detect encoding and removed from the
     * data)
     * @return parsed Java instance
     */
    fun loadFromInputStream(yamlStream: InputStream): Any? {
        return loadOne(createComposer(yamlStream))
    }

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yamlReader - data to load from (BOM must not be present)
     * @return parsed Java instance
     */
    fun loadFromReader(yamlReader: Reader): Any? = loadOne(createComposer(yamlReader))

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yaml - YAML data to load from (BOM must not be present)
     * @return parsed Java instance
     * @throws org.snakeyaml.engine.v2.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadFromString(yaml: String): Any? = loadOne(createComposer(yaml))

    // Load all the documents
    private fun loadAll(composer: Composer): Iterable<Any?> =
        Iterable { YamlIterator(composer, constructor) }

    /**
     * Parse all YAML documents in a stream and produce corresponding Java objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param yamlStream - YAML data to load from (BOM is respected to detect encoding and removed
     * from the data)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    fun loadAllFromInputStream(yamlStream: InputStream): Iterable<Any?> {
        val composer = createComposer(StreamReader(settings, YamlUnicodeReader(yamlStream.source())))
        return loadAll(composer)
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param yamlReader - YAML data to load from (BOM must not be present)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    fun loadAllFromReader(yamlReader: Reader): Iterable<Any?> {
        val composer = createComposer(StreamReader(settings, yamlReader.readText()))
        return loadAll(composer)
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java objects. (Because the
     * encoding in known BOM is not respected.) The documents are parsed only when the iterator is
     * invoked.
     *
     * @param yaml - YAML data to load from (BOM must not be present)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    fun loadAllFromString(yaml: String): Iterable<Any?> {
        val composer = createComposer(StreamReader(settings, yaml))
        return loadAll(composer)
    }

    private class YamlIterator(
        private val composer: Composer,
        private val constructor: BaseConstructor,
    ) : MutableIterator<Any?> {
        private var composerInitiated = false
        override fun hasNext(): Boolean {
            composerInitiated = true
            return composer.hasNext()
        }

        override fun next(): Any? {
            if (!composerInitiated) {
                hasNext()
            }
            val node = composer.next()
            return constructor.constructSingleDocument(node)
        }

        override fun remove(): Unit = throw UnsupportedOperationException("Removing is not supported.")
    }
}
