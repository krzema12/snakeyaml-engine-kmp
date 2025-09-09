package it.krzeminski.snakeyaml.engine.kmp.events

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ImplicitTupleTest : FunSpec({
    test("toString") {
        ImplicitTuple(plain = true, nonPlain = false).toString() shouldBe "implicit=[true, false]"
    }
})
