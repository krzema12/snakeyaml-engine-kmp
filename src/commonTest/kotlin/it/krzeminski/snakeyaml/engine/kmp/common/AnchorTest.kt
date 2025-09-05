package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException

class AnchorTest : FunSpec({

    test("Anchor cannot be empty") {
        val exception = shouldThrow<IllegalArgumentException> {
            Anchor("")
        }
        exception.message shouldBe "Empty anchor."
    }

    test("Anchor cannot contain a space") {
        val exception = shouldThrow<EmitterException> {
            Anchor("an chor")
        }
        exception.message shouldBe "Anchor may not contain spaces: an chor"
    }

    test("Anchor cannot contains some characters") {
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
