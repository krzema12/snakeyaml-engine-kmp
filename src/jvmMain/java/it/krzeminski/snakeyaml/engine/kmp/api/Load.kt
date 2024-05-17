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

import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import it.krzeminski.snakeyaml.engine.kmp.constructor.StandardConstructor
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.Source
import okio.source
import java.io.InputStream
import java.io.Reader

/**
 * Common way to load object instance(s).
 *
 * This class is not thread-safe. Which means that all the methods of the same instance can be
 * called only by one thread. It is better to create an instance for every YAML stream. The
 * instance is stateful. Only one of the 'load' methods may be called, and it may be called only once.
 *
 * @param settings configuration
 * @param constructor custom YAML constructor
 */
class Load @JvmOverloads constructor(
    private val settings: LoadSettings,
    private val constructor: BaseConstructor = StandardConstructor(settings),
) {

    /** Create a new [Composer] from [yaml], using [settings]. */
    private fun createComposer(yaml: Source): Composer {
        val reader = StreamReader(loadSettings = settings, stream = YamlUnicodeReader(yaml))
        return Composer(settings, ParserImpl(settings, reader))
    }

    /** Create a new [Composer] from [inputStream], using [settings]. */
    private fun createComposer(inputStream: InputStream): Composer = createComposer(inputStream.source())

    /** Create a new [Composer] from [string], using [settings]. */
    private fun createComposer(string: String): Composer = createComposer(string.byteInputStream())

    /** Create a new [Composer] from [reader], using [settings]. */
    private fun createComposer(reader: Reader): Composer = createComposer(reader.readText())

    /**
     * Load a single document with the provided [composer]
     *
     * @param composer the component to create the Node
     * @return decoded YAML document
     */
    private fun loadOne(composer: Composer): Any? {
        val nodeOptional = composer.getSingleNode()
        return constructor.constructSingleDocument(nodeOptional)
    }

    /**
     * Parse the only YAML document in a stream and produce the corresponding object.
     *
     * @param inputStream data to load from (BOM is respected to detect encoding and removed from the data)
     * @return parsed object instance
     */
    fun loadOne(inputStream: InputStream): Any? = loadOne(createComposer(inputStream))

    /**
     * Parse a YAML document and create a object instance.
     *
     * @param reader data to load from (BOM must not be present)
     * @return parsed object instance
     */
    fun loadOne(reader: Reader): Any? = loadOne(createComposer(reader))

    /**
     * Parse a YAML document and create an instance of an object.
     *
     * @param yaml YAML data to load from (BOM must not be present)
     * @return parsed instance
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadOne(string: String): Any? = loadOne(createComposer(string))

    /** Load all the documents. */
    private fun loadAll(composer: Composer): Iterable<Any?> =
        Iterable { YamlIterator(composer, constructor) }

    /**
     * Parse all YAML documents in a stream and produce corresponding objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param inputStream YAML data to load from (BOM is respected to detect encoding and removed
     * from the data)
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(inputStream: InputStream): Iterable<Any?> = loadAll(createComposer(inputStream))

    /**
     * Parse all YAML documents in a String and produce corresponding objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param reader YAML data to load from (BOM must not be present)
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(reader: Reader): Iterable<Any?> = loadAll(createComposer(reader))

    /**
     * Parse all YAML documents in a String and produce corresponding objects. (Because the
     * encoding in known BOM is not respected.) The documents are parsed only when the iterator is
     * invoked.
     *
     * @param string YAML data to load from (BOM must not be present)
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(string: String): Iterable<Any?> = loadAll(createComposer(string))


    private class YamlIterator(
        private val composer: Composer,
        private val constructor: BaseConstructor,
    ) : Iterator<Any?> {
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
    }

    @Deprecated("renamed", ReplaceWith("loadAll(yamlStream)"))
    fun loadAllFromInputStream(yamlStream: InputStream): Iterable<Any?> =
        loadAll(yamlStream)

    @Deprecated("renamed", ReplaceWith("loadAll(yaml)"))
    fun loadAllFromString(yaml: String): Iterable<Any?> = loadAll(yaml)

    @Deprecated("renamed", ReplaceWith("loadAll(yamlReader)"))
    fun loadAllFromReader(yamlReader: Reader): Iterable<Any?> = loadAll(yamlReader)

    @Deprecated("renamed", ReplaceWith("loadOne(yamlStream)"))
    fun loadFromInputStream(yamlStream: InputStream): Any? = loadOne(yamlStream)

    @Deprecated("renamed", ReplaceWith("loadOne(yamlReader)"))
    fun loadFromReader(yamlReader: Reader): Any? = loadOne(yamlReader)

    @Deprecated("renamed", ReplaceWith("loadOne(yamlReader)"))
    fun loadFromString(yaml: String): Any? = loadOne(yaml)
}
