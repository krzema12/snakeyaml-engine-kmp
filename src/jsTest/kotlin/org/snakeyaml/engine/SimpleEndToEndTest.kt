package org.snakeyaml.engine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SimpleEndToEndTest : FunSpec({
    test("simple case") {
        1 + 1 shouldBe 2
    }
})
