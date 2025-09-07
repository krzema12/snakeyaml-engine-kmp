package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException

class AnchorTest : FunSpec({

    test("anchor cannot be empty") {
        shouldThrowWithMessage<IllegalArgumentException>(message = "Empty anchor.") {
            Anchor("")
        }
    }

    test("anchor cannot contain a space") {
        shouldThrowWithMessage<EmitterException>(message = "Anchor may not contain spaces: an chor") {
            Anchor("an chor")
        }
    }

    test("anchor cannot contains some characters") {
        checkChar('[').message shouldBe "Invalid character '[' in the anchor: anchor["
        checkChar(']').message shouldBe "Invalid character ']' in the anchor: anchor]"
        checkChar('{').message shouldBe "Invalid character '{' in the anchor: anchor{"
        checkChar('}').message shouldBe "Invalid character '}' in the anchor: anchor}"
        checkChar('*').message shouldBe "Invalid character '*' in the anchor: anchor*"
        checkChar('&').message shouldBe "Invalid character '&' in the anchor: anchor&"
    }
})

private fun checkChar(ch: Char): EmitterException {
    return shouldThrow<EmitterException> {
        Anchor("anchor$ch")
    }
}
