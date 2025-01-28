package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.scanner.Scanner
import it.krzeminski.snakeyaml.engine.kmp.scanner.ScannerImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.tokens.ScalarToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token.ID

class ScannerWithCommentEnabledTest: FunSpec({
    test("empty") {
        val expected = listOf(ID.StreamStart, ID.StreamEnd)

        val sut = constructScanner("")

        assertTokensEqual(expectedList = expected, sut = sut)
    }

    test("only comment lines") {
        val expected = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.Comment,
            ID.StreamEnd,
        )

        val sut = constructScanner(
            """
            # This stream contains no
            # documents, only comments.
            """.trimIndent()
        )

        assertTokensEqual(expectedList = expected, sut = sut)
    }

    test("comment ending a line") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment,
            ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("key", "value")

        val sut = constructScanner(
            """
            key: # Comment
              value
            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("multiline comment") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Comment,
            ID.Scalar,
            ID.Comment,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("key", "value")

        val sut = constructScanner(
            """
            key: # Comment
                 # lines
              value


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("blank line") {
        val expected = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.StreamEnd)

        val sut = constructScanner("\n")

        assertTokensEqual(expectedList = expected, sut = sut)
    }

    test("blank line comments") {
        val expected = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Scalar, ID.Comment,
            ID.Comment,
            ID.Comment,
            ID.BlockEnd,
            ID.StreamEnd,
        )

        val sut = constructScanner(
            """

            abc: def # comment



            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            sut = sut,
        )
    }

    test("block scalar - replace new line with spaces - single new line at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment,
            ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def hij\n")

        val sut = constructScanner(
            """
            abc: > # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("block scalar - replace new line with spaces - no new line at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def hij")

        val sut = constructScanner(
            """
            abc: >- # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("block scalar - replace new line with spaces - all new lines at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def hij\n\n")

        val sut = constructScanner(
            """
            abc: >+ # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("block scalar - keep new line - single new line at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def\nhij\n")

        val sut = constructScanner(
            """
            abc: | # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("block scalar - keep new line - no new line at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def\nhij")

        val sut = constructScanner(
            """
            abc: |- # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("block scalar - keep new line - all new lines at and") {
        val expected = listOf(
            ID.StreamStart,
            ID.BlockMappingStart,
            ID.Key, ID.Scalar, ID.Value, ID.Comment, ID.Scalar,
            ID.BlockEnd,
            ID.StreamEnd,
        )
        val expectedScalarValue = listOf("abc", "def\nhij\n\n")

        val sut = constructScanner(
            """
            abc: |+ # Comment
                def
                hij


            """.trimIndent()
        )

        assertTokensEqual(
            expectedList = expected,
            expectedScalarValueList = expectedScalarValue,
            sut = sut,
        )
    }

    test("directive line end comment") {
        val expected = listOf(
            ID.StreamStart,
            ID.Directive,
            ID.Comment,
            ID.StreamEnd,
        )

        val sut = constructScanner("%YAML 1.1 #Comment\n")

        assertTokensEqual(
            expectedList = expected,
            sut = sut,
        )
    }
})

private fun constructScanner(input: String): Scanner {
    val settings = LoadSettings.builder().setParseComments(true).build()
    return ScannerImpl(settings, StreamReader(settings, input))
}

private fun assertTokensEqual(
    expectedList: List<ID>,
    expectedScalarValueList: List<String>? = null,
    sut: Scanner,
) {
    val expectedIterator = expectedList.iterator()
    val expectedScalarValueIterator = expectedScalarValueList?.iterator()

    while (!sut.checkToken(ID.StreamEnd)) {
        val token = sut.next()
        assertTokenEquals(
            expectedIdIterator = expectedIterator,
            expectedScalarValueIterator = expectedScalarValueIterator,
            token = token,
        )
    }

    val token = sut.peekToken()
    assertTokenEquals(
        expectedIdIterator = expectedIterator,
        expectedScalarValueIterator = expectedScalarValueIterator,
        token = token,
    )
    withClue("unexpected tokens") {
        expectedIterator.hasNext().shouldBeFalse()
    }
}

private fun assertTokenEquals(
    expectedIdIterator: Iterator<ID>,
    expectedScalarValueIterator: Iterator<String>?,
    token: Token,
) {
    expectedIdIterator.hasNext().shouldBeTrue()
    val expectedValue = expectedIdIterator.next()
    val id = token.tokenId
    expectedValue shouldBeSameInstanceAs id

    if (expectedScalarValueIterator != null && id == ID.Scalar) {
        expectedScalarValueIterator.next() shouldBe (token as ScalarToken).value
    }
}
