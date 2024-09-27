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

import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
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
actual class Load @JvmOverloads actual constructor(
    private val settings: LoadSettings,
    private val constructor: BaseConstructor,
) {
    private val common = LoadCommon(settings, constructor)

    actual fun loadOne(string: String): Any? =
        common.loadOne(string)

    internal actual fun loadOne(source: Source): Any? =
        common.loadOne(source)

    /**
     * Parse the only YAML document in a stream and produce the corresponding object.
     *
     * @param inputStream data to load from (BOM is respected to detect encoding and removed from the data)
     * @return parsed object instance
     */
    fun loadOne(inputStream: InputStream): Any? =
        common.loadOne(inputStream.source())

    /**
     * Parse a YAML document and create a object instance.
     *
     * @param reader data to load from
     * @return parsed object instance
     */
    fun loadOne(reader: Reader): Any? =
        common.loadOne(reader.readText())

    actual fun loadAll(string: String): Iterable<Any?> =
        common.loadAll(string)

    internal  actual fun loadAll(source: Source): Iterable<Any?> =
        common.loadAll(source)

    /**
     * Parse all YAML documents in a stream and produce corresponding objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param inputStream YAML data to load from (BOM is respected to detect encoding and removed
     * from the data)
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(inputStream: InputStream): Iterable<Any?> =
        common.loadAll(inputStream.source())

    /**
     * Parse all YAML documents in a String and produce corresponding objects. The documents are
     * parsed only when the iterator is invoked.
     *
     * @param reader YAML data to load from
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(reader: Reader): Iterable<Any?> =
        common.loadAll(reader.readText())

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
