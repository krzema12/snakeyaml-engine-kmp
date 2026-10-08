package it.krzeminski.snakeyaml.engine.kmp.issues.issue95

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

/**
 * Ported from issue 95 in SnakeYAML Engine.
 *
 * An in-line comment on a document marker ("---" or "...") with no following document
 * must not throw when loading all documents in a stream.
 */
class CommentOnDocumentMarkerAtEndOfStreamTest : FunSpec({
    val load = Load(LoadSettings(parseComments = true))

    fun loadAll(yaml: String): List<Any?> = load.loadAll(yaml).toList()

    test("in-line comment on the document start marker, no next document") {
        loadAll("--- foo ###\n") shouldBe listOf("foo")
    }

    test("in-line comment on the document end marker, no next document") {
        loadAll("foo: 1\n... ###\n") shouldBe listOf(mapOf("foo" to 1))
    }

    test("in-line comment on the document end marker, CRLF line endings") {
        loadAll("foo: 1\r\n... ###\r\n") shouldBe listOf(mapOf("foo" to 1))
    }

    test("in-line comment on the document start marker, CRLF line endings") {
        loadAll("--- foo ###\r\n") shouldBe listOf("foo")
    }

    test("in-line comment on the document start marker, quoted scalar") {
        loadAll("--- \"foo\" ###\n") shouldBe listOf("foo")
    }

    test("in-line comment on the document start marker, tagged scalar") {
        loadAll("--- !!str 1 ###\n") shouldBe listOf("1")
    }

    test("in-line comment on the document start marker, anchored scalar") {
        loadAll("--- &a foo ###\n") shouldBe listOf("foo")
    }

    test("single-document API is unaffected") {
        load.loadOne("--- foo ###\n") shouldBe "foo"
        load.loadOne("foo: 1\n... ###\n") shouldBe mapOf("foo" to 1)
    }
})
