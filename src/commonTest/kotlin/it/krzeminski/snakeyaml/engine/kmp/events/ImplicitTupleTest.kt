package it.krzeminski.snakeyaml.engine.kmp.events

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ImplicitTupleTest : FunSpec({
    test("testToString") {
        ImplicitTuple(true, false).toString() shouldBe "implicit=[true, false]"
    }
})
