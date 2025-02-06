package it.krzeminski.snakeyaml.engine.kmp.resolver

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.regex.shouldMatch
import io.kotest.matchers.regex.shouldNotMatch
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class CoreScalarResolverTest: FunSpec({
    test("resolve implicit integer") {
        assertSoftly {
            CoreScalarResolver.INT shouldMatch "0o1010"
            CoreScalarResolver.INT shouldNotMatch "0b1010"

            CoreScalarResolver().resolve("0b1010", implicit = true) shouldBe Tag.STR
        }
    }
})
