package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow

class UriEncoderTest : FunSpec({

    test("Encode-decode") {
        val encoded = UriEncoder.encode(" +%")
        encoded shouldBe "%20%2B%25"
        val decoded = UriEncoder.decode(encoded)
        decoded shouldBe " +%"
    }

    test("Invalid decode") {
        val exception = shouldThrow<IllegalArgumentException> {
            UriEncoder.decode("%2")
        }
        exception.message shouldBe "Incomplete trailing escape (%) pattern"
    }
})
