package it.krzeminski.snakeyaml.engine.kmp.api

import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.constructor.BaseConstructor
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.Buffer
import okio.Source

/**
 * Kotlin Common implementations of [it.krzeminski.snakeyaml.engine.kmp.api.Load] functions.
 *
 * @see it.krzeminski.snakeyaml.engine.kmp.api.Load
 */
internal class LoadCommon(
    private val settings: LoadSettings,
    private val constructor: BaseConstructor,
) {
    /** Create a new [Composer] from [source], using [settings]. */
    private fun createComposer(source: Source): Composer {
        val reader = StreamReader(loadSettings = settings, stream = YamlUnicodeReader(source))
        return Composer(settings, ParserImpl(settings, reader))
    }

    /** Create a new [Composer] from [string], using [settings]. */
    private fun createComposer(string: String): Composer =
        createComposer(Buffer().writeUtf8(string))

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
     * Parse a YAML document and create an instance of an object.
     *
     * The string must be encoded as UTF-8.
     *
     * @param string YAML data to load from (BOM must not be present)
     * @return parsed instance
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadOne(string: String): Any? =
        loadOne(createComposer(string))

    /**
     * Parse a YAML document and create an instance of an object.
     *
     * @param source YAML data to load
     * @return parsed instance
     * @throws it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException if the YAML is not valid
     */
    fun loadOne(source: Source): Any? =
        loadOne(createComposer(source))

    /** Load all the documents. */
    private fun loadAll(composer: Composer): Iterable<Any?> =
        Iterable { YamlIterator(composer, constructor) }

    /**
     * Parse all YAML documents in a String and produce corresponding objects. (Because the
     * encoding in known BOM is not respected.) The documents are parsed only when the iterator is
     * invoked.
     *
     * The string must be encoded as UTF-8.
     *
     * @param string YAML data to load from (BOM must not be present)
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(string: String): Iterable<Any?> =
        loadAll(createComposer(string))

    /**
     * Parse all YAML documents in a [Source] and produce corresponding objects.
     * The documents are parsed only when the iterator is invoked.
     *
     * @param source YAML data to load.
     * @return an [Iterable] over the parsed objects in this stream in proper sequence
     */
    fun loadAll(source: Source): Iterable<Any?> =
        loadAll(createComposer(source))
}

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
