package it.krzeminski.snakeyaml.engine.kmp.issues.issue84

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

/**
 * Issue 84: an in-line comment on a document separator ("---" or "...") must not break loading.
 */
class CommentOnDocumentMarkerTest : FunSpec({
    val load = Load(LoadSettings(parseComments = true))

    test("Issue 84: comment on the document start marker") {
        load.loadOne("--- # This comment causes exception\nkey: value") shouldBe mapOf("key" to "value")
    }

    test("Issue 84: document is only a marker with a comment") {
        load.loadOne("--- # just a comment\n") shouldBe null
    }

    test("Issue 84: comment on the document start marker before a sequence") {
        load.loadOne("--- # Comment\n- a\n- b\n") shouldBe listOf("a", "b")
    }

    test("Issue 84: comment on the document start marker before a flow mapping") {
        load.loadOne("--- # Comment\n{a: 1}\n") shouldBe mapOf("a" to 1)
    }

    test("Issue 84: comment on the document separator between two documents") {
        load.loadAll("a: 1\n--- # Comment\nb: 2\n").toList() shouldBe listOf(mapOf("a" to 1), mapOf("b" to 2))
    }

    test("Issue 84: comment on the document end marker before the next document start") {
        load.loadAll("a: 1\n... # end comment\n--- # start comment\nb: 2\n").toList() shouldBe
            listOf(mapOf("a" to 1), mapOf("b" to 2))
    }
})
