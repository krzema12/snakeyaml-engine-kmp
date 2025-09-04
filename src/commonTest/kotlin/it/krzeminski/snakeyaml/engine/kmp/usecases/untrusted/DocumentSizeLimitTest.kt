package it.krzeminski.snakeyaml.engine.kmp.usecases.untrusted

import io.kotest.core.spec.style.FunSpec
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
        val settings1 = LoadSettings.builder().setCodePointLimit(8).build()
        val load1 = Load(settings1)
        val doc = "---\nfoo\n---\nbar\n"
        val iter1 = load1.loadAll(doc).iterator()
        iter1.next() shouldBe "foo"
        iter1.next() shouldBe "bar"
        iter1.hasNext() shouldBe false

        // exceed the limit
        val settings2 = LoadSettings.builder().setCodePointLimit(8 - 1).build()
        val load2 = Load(settings2)
        val iter2 = load2.loadAll(doc).iterator()
        iter2.next() shouldBe "foo"
        try {
            iter2.next()
        } catch (e: YamlEngineException) {
            // There's a bug in the test in snakeyaml-engine (source of this ported test), and the above line doesn't throw this exception (at least in some cases)
            // TODO: report it to snakeyaml-engine's owner: https://github.com/krzema12/snakeyaml-engine-kmp/issues/541
            e.message shouldBe "The incoming YAML document exceeds the limit: 4 code points."
        }
    }

    test("last load many documents") {
        /**
         * The document start '---\n' is added to the non-first documents.
         */
        val secondDocument = "---\nbar\n"
        val limit = secondDocument.length
        val settings1 = LoadSettings.builder().setCodePointLimit(limit).build()
        val load1 = Load(settings1)
        val complete = "foo\n$secondDocument"
        val iter1 = load1.loadAll(complete).iterator()
        iter1.next() shouldBe "foo"
        iter1.next() shouldBe "bar"
        iter1.hasNext() shouldBe false

        // exceed the limit
        val settings2 = LoadSettings.builder().setCodePointLimit(limit - 1).build()
        val load2 = Load(settings2)
        val iter2 = load2.loadAll(complete).iterator()
        iter2.next() shouldBe "foo"
        try {
            // There's a bug in the test in snakeyaml-engine (source of this ported test), and the above line doesn't throw this exception (at least in some cases)
            // TODO: report it to snakeyaml-engine's owner: https://github.com/krzema12/snakeyaml-engine-kmp/issues/541
            iter2.next()
        } catch (e: YamlEngineException) {
            e.message shouldBe "The incoming YAML document exceeds the limit: 4 code points."
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
    val settings1 = LoadSettings.builder().setCodePointLimit(codePointLimit).build()
    val load = Load(settings1)
    val docs = load.loadAll(input).iterator()
    for (ndx in 1..3) {
        try {
            docs.next()
        } catch (e: Exception) {
            return false
        }
    }
    return true
}
