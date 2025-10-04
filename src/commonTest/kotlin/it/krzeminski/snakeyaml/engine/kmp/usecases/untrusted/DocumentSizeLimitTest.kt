package it.krzeminski.snakeyaml.engine.kmp.usecases.untrusted

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * https://bitbucket.org/snakeyaml/snakeyaml/issues/1065
 */
class DocumentSizeLimitTest : FunSpec({
    test("first load many documents") {
        /**
         * The document start '---\n' is added to the first document
         */
        val settings1 = LoadSettings(codePointLimit = 8)
        val load1 = Load(settings1)
        val doc = "---\nfoo\n---\nbarbar\n"
        val iter1 = load1.loadAll(doc).iterator()
        iter1.next() shouldBe "foo"
        iter1.next() shouldBe "barbar"
        iter1.hasNext() shouldBe false

        // Exceed the limit by 1
        val settings2 = LoadSettings(codePointLimit = 8 - 1)
        val load2 = Load(settings2)
        val iter2 = load2.loadAll(doc).iterator()
        iter2.next() shouldBe "foo" // the first document is loaded
        // The second document should fail because of the limit
        shouldThrowWithMessage<YamlEngineException>(message = "The incoming YAML document exceeds the limit: 7 code points.") {
            iter2.next()
        }
    }

    test("last load many documents") {
        /**
         * The document start '---\n' is added to the non-first documents.
         * Document indicators affect the limit ('---' and '...')
         */
        val settings1 = LoadSettings(codePointLimit = 7)
        val load1 = Load(settings1)
        val complete = "foo\n...\n---\nbar\n"
        val iter1 = load1.loadAll(complete).iterator()
        iter1.next() shouldBe "foo"
        iter1.next() shouldBe "bar"
        iter1.hasNext() shouldBe false

        // exceed the limit
        val settings2 = LoadSettings(codePointLimit = 6)
        val load2 = Load(settings2)
        val iter2 = load2.loadAll(complete).iterator()
        iter2.next() shouldBe "foo" // the first document is loaded
        // Second doc should fail because of the limit
        shouldThrowWithMessage<YamlEngineException>(message = "The incoming YAML document exceeds the limit: 6 code points.") {
            iter2.next()
        }
    }

    test("load documents") {
        val doc1 = "document: this is document one\n"
        val doc2 = "---\ndocument: this is document 2\n"
        val docLongest = "---\ndocument: this is document three\n"
        val input = doc1 + doc2 + docLongest

        dumpAllDocs(input, input.length) shouldBe true
        dumpAllDocs(input, docLongest.length) shouldBe true
        dumpAllDocs(input, doc2.length) shouldBe false
    }
})

private fun dumpAllDocs(input: String, codePointLimit: Int): Boolean {
    val settings1 = LoadSettings(codePointLimit = codePointLimit)
    val load = Load(settings1)
    val docs = load.loadAll(input).iterator()
    for (ndx in 1..3) {
        try {
            val doc = docs.next()
            doc.shouldNotBeNull()
        } catch (e: Exception) {
            return false
        }
    }
    return true
}
