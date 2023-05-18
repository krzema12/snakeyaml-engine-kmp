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

import org.snakeyaml.engine.v2.composer.Composer
import org.snakeyaml.engine.v2.constructor.BaseConstructor
import org.snakeyaml.engine.v2.constructor.StandardConstructor
import org.snakeyaml.engine.v2.parser.ParserImpl
import org.snakeyaml.engine.v2.scanner.StreamReader
import java.io.InputStream
import java.io.Reader
import java.util.Objects
import java.util.Optional

/**
 * Common way to load Java instance(s). This class is not thread-safe. Which means that all the
 * methods of the same instance can be called only by one thread. It is better to create an instance
 * for every YAML stream. The instance is stateful. Only one of the 'load' methods may be called,
 * and it may be called only once.
 */
class Load @JvmOverloads constructor(
    settings: LoadSettings,
    constructor: BaseConstructor = StandardConstructor(settings)
) {
    private val settings: LoadSettings
    private val constructor: BaseConstructor
    /**
     * Create instance to parse the incoming YAML data and create Java instances
     *
     * @param settings - configuration
     * @param constructor - custom YAML constructor
     */
    /**
     * Create instance to parse the incoming YAML data and create Java instances
     *
     * @param settings - configuration
     */
    init {
        Objects.requireNonNull(settings, "LoadSettings cannot be null")
        Objects.requireNonNull(constructor, "BaseConstructor cannot be null")
        this.settings = settings
        this.constructor = constructor
    }

    /**
     * Create Composer
     *
     * @param streamReader - the input
     * @return configured Composer
     */
    private fun createComposer(streamReader: StreamReader): Composer {
        return Composer(settings, ParserImpl(settings, streamReader))
    }

    /**
     * Create Composer
     *
     * @param yamlStream - the input
     * @return configured Composer
     */
    protected fun createComposer(yamlStream: InputStream?): Composer {
        return createComposer(StreamReader(settings, YamlUnicodeReader(yamlStream)))
    }

    /**
     * Create Composer
     *
     * @param yaml - the input
     * @return configured Composer
     */
    protected fun createComposer(yaml: String?): Composer {
        return createComposer(StreamReader(settings, yaml))
    }

    /**
     * Create Composer
     *
     * @param yamlReader - the input
     * @return configured Composer
     */
    protected fun createComposer(yamlReader: Reader?): Composer {
        return createComposer(StreamReader(settings, yamlReader))
    }
    // Load a single document
    /**
     * Load with provided Composer
     *
     * @param composer - the component to create the Node
     * @return deserialised YAML document
     */
    protected fun loadOne(composer: Composer): Any {
        val nodeOptional = composer.singleNode
        return constructor.constructSingleDocument(nodeOptional)
    }

    /**
     * Parse the only YAML document in a stream and produce the corresponding Java object.
     *
     * @param yamlStream - data to load from (BOM is respected to detect encoding and removed from the
     * data)
     * @return parsed Java instance
     */
    fun loadFromInputStream(yamlStream: InputStream): Any {
        Objects.requireNonNull(yamlStream, "InputStream cannot be null")
        return loadOne(createComposer(yamlStream))
    }

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yamlReader - data to load from (BOM must not be present)
     * @return parsed Java instance
     */
    fun loadFromReader(yamlReader: Reader): Any {
        Objects.requireNonNull(yamlReader, "Reader cannot be null")
        return loadOne(createComposer(yamlReader))
    }

    /**
     * Parse a YAML document and create a Java instance
     *
     * @param yaml - YAML data to load from (BOM must not be present)
     * @return parsed Java instance
     * @throws org.snakeyaml.engine.v2.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadFromString(yaml: String): Any {
        Objects.requireNonNull(yaml, "String cannot be null")
        return loadOne(createComposer(yaml))
    }

    // Load all the documents
    private fun loadAll(composer: Composer): Iterable<Any> {
        val result: MutableIterator<Any> = YamlIterator(composer, constructor)
        return YamlIterable(result)
    }

    /**
     * Parse all YAML documents in a stream and produce corresponding Java objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param yamlStream - YAML data to load from (BOM is respected to detect encoding and removed
     * from the data)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    fun loadAllFromInputStream(yamlStream: InputStream): Iterable<Any> {
        Objects.requireNonNull(yamlStream, "InputStream cannot be null")
        val composer = createComposer(StreamReader(settings, YamlUnicodeReader(yamlStream)))
        return loadAll(composer)
    }

    /**
     * Parse all YAML documents in a String and produce corresponding Java objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param yamlReader - YAML data to load from (BOM must not be present)
     * @return an Iterable over the parsed Java objects in this stream in proper sequence
     */
    fun loadAllFromReader(yamlReader: Reader): Iterable<Any> {
        Objects.requireNonNull(yamlReader, "Reader cannot be null")
        val composer = createComposer(StreamReader(settings, yamlReader))
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
    fun loadAllFromString(yaml: String): Iterable<Any> {
        Objects.requireNonNull(yaml, "String cannot be null")
        val composer = createComposer(StreamReader(settings, yaml))
        return loadAll(composer)
    }

    private class YamlIterable(private val iterator: MutableIterator<Any>) : Iterable<Any> {
        override fun iterator(): MutableIterator<Any> {
            return iterator
        }
    }

    private class YamlIterator(private val composer: Composer, private val constructor: BaseConstructor) :
        MutableIterator<Any> {
        private var composerInitiated = false
        override fun hasNext(): Boolean {
            composerInitiated = true
            return composer.hasNext()
        }

        override fun next(): Any {
            if (!composerInitiated) {
                hasNext()
            }
            val node = composer.next()
            return constructor.constructSingleDocument(Optional.of(node))
        }

        override fun remove() {
            throw UnsupportedOperationException("Removing is not supported.")
        }
    }
}