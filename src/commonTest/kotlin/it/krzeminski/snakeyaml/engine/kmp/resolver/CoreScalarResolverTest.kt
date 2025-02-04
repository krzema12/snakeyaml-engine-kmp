package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class CoreScalarResolverTest: FunSpec({
    test("resolve implicit integer") {
        CoreScalarResolver.INT.matches("0o1010") shouldBe true
        CoreScalarResolver.INT.matches("0b1010") shouldBe false

        CoreScalarResolver().resolve("0b1010", implicit = true) shouldBe Tag.STR
    }
})
