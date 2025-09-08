package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SimpleKeyTest : FunSpec({

    test("toString should format simple key correctly") {
        val simpleKey = SimpleKey(0, true, 0, 0, 0, null)
        simpleKey.toString() shouldBe "SimpleKey - tokenNumber=0 required=true index=0 line=0 column=0"
    }
})
