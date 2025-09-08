package it.krzeminski.snakeyaml.engine.kmp.tokens

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TokenTest : FunSpec({

    test("toString should format scalar token correctly") {
        val token: Token = ScalarToken("a", true, null, null)
        token.toString() shouldBe "<scalar> plain=true style=: value=a"
    }
})
