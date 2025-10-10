package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.tokens.AnchorToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.ScalarToken
import it.krzeminski.snakeyaml.engine.kmp.tokens.Token

class ScannerTest : FunSpec({

    test("expected NoSuchElementException after all the tokens are finished") {
        val settings = LoadSettings()
        val reader = StreamReader(settings, "444222")
        val scanner = ScannerImpl(settings, reader)

        scanner.hasNext().shouldBeTrue()
        scanner.next().tokenId shouldBe Token.ID.StreamStart
        scanner.hasNext().shouldBeTrue()
        scanner.next().tokenId shouldBe Token.ID.Scalar
        scanner.hasNext().shouldBeTrue()
        scanner.next().tokenId shouldBe Token.ID.StreamEnd
        scanner.hasNext().shouldBeFalse()

        shouldThrowWithMessage<NoSuchElementException>(message = "No more Tokens found.") {
            scanner.next()
        }
    }

    test("652Z: ? is part of the key if no space after it") {
        val token = scanTo("{ ?foo: bar }", 3)
        token.tokenId shouldBe Token.ID.Scalar
        val scalar = token as ScalarToken
        scalar.value shouldBe "?foo"
    }

    test("Y2GN: anchor may contain colon ':'") {
        val token = scanTo("key: &an:chor value", 5)
        token.tokenId shouldBe Token.ID.Anchor
        val anchorToken = token as AnchorToken
        anchorToken.value.value shouldBe "an:chor"
    }
})

private fun scanTo(input: String, skip: Int): Token {
    val settings = LoadSettings()
    val reader = StreamReader(settings, input)
    val scanner = ScannerImpl(settings, reader)
    var i = 0
    while (i < skip) {
        scanner.hasNext().shouldBeTrue()
        val token = scanner.next()
        token shouldNotBe null
        i++
    }
    scanner.hasNext().shouldBeTrue()
    return scanner.next()
}
