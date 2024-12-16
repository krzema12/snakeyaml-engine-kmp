package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import it.krzeminski.snakeyaml.engine.kmp.constructor.StandardConstructor
import okio.Source

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
expect class Load(
    settings: LoadSettings = LoadSettings.builder().build(),
    constructor: BaseConstructor = StandardConstructor(settings),
) {
    /**
     * Parse a YAML document and create an instance of an object.
     *
     * @param string YAML data to load from
     * @return parsed instance
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadOne(string: String): Any?

    /**
     * Parse a YAML document and create an instance of an object.
     *
     * @param source YAML data to load
     * @return parsed instance
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException if the YAML is not valid
     */
    // internal visibility because Okio is not a required dependency
    internal fun loadOne(source: Source): Any?

    /**
     * Parse all YAML documents in a String and produce corresponding objects.
     * The documents are parsed only when the iterator is invoked.
     *
     * @param string YAML data to load from
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(string: String): Iterable<Any?>

    /**
     * Parse all YAML documents in a [Source] and produce corresponding objects.
     * The documents are parsed only when the iterator is invoked.
     *
     * @param source YAML data to load.
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    // internal visibility because Okio is not a required dependency
    internal fun loadAll(source: Source): Iterable<Any?>
}
