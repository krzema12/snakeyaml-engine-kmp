package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrowWithMessage

class UriEncoderTest : FunSpec({

    test("encode-decode") {
        val encoded = UriEncoder.encode(" +%")
        encoded shouldBe "%20%2B%25"
        val decoded = UriEncoder.decode(encoded)
        decoded shouldBe " +%"
    }

    test("invalid decode") {
        shouldThrowWithMessage<IllegalArgumentException>(message = "Incomplete trailing escape (%) pattern") {
            UriEncoder.decode("%2")
        }
    }
})
