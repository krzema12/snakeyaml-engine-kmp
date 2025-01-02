package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ReaderException

class ReaderStringTest : FunSpec({
    test("check printable") {
        val reader = StreamReader(LoadSettings.builder().build(), "test")
        reader.peek(4) shouldBe 0
        StreamReader.isPrintable("test") shouldBe true
    }

    test("check non-printable") {
        StreamReader.isPrintable("test\u0005 fail") shouldBe false
        shouldThrow<ReaderException> {
            val reader = StreamReader(LoadSettings.builder().build(), "test\u0005 fail")
            while (reader.peek() != 0) {
                reader.forward()
            }
        }.also {
            it.toString() shouldBe "unacceptable code point '\u0005' (0x5) special characters are not allowed\nin \"reader\", position 4"
        }
    }

    test("check all") {
        var highSurrogatesCounter = 0
        var lowSurrogatesCounter = 0
        for (i in 0..<256 * 256) {
            if (i.toChar().isHighSurrogate()) {
                highSurrogatesCounter++
            } else if (i.toChar().isLowSurrogate()) {
                lowSurrogatesCounter++
            } else {
                val str = CharArray(1) { i.toChar() }.concatToString()
                val regularExpressionResult = StreamReader.isPrintable(str)

                var charsArrayResult = true
                try {
                    StreamReader(LoadSettings.builder().build(), str).peek()
                } catch (e: Exception) {
                    (
                        (e.message?.startsWith("unacceptable character") ?: false)
                            || e.message == "special characters are not allowed"
                    ) shouldBe true
                    charsArrayResult = false
                }
                withClue("Failed for #$i") {
                    regularExpressionResult shouldBe charsArrayResult
                }
            }
        }
        // https://en.wikipedia.org/wiki/Universal_Character_Set_characters
        withClue("There are 1024 low surrogates (D800–DBFF)") {
            highSurrogatesCounter shouldBe 1024
        }
        withClue("There are 1024 high surrogates (DC00–DFFF)") {
            lowSurrogatesCounter shouldBe 1024
        }
    }

    test("high surrogate alone") {
        val reader = StreamReader(LoadSettings.builder().build(), "test\uD800")
        try {
            while (reader.peek() != 0) {
                reader.forward()
            }
        } catch(e: ReaderException) {
            e.toString() shouldContain "(0xD800) The last char is HighSurrogate (no LowSurrogate detected)"
            e.position shouldBe 5
        }
    }

    test("forward") {
        val reader = StreamReader(LoadSettings.builder().build(), "test")
        while (reader.peek() != 0) {
            reader.forward(1)
        }
        val reader2 = StreamReader(LoadSettings.builder().build(), "test")
        reader2.peek() shouldBe 't'.code
        reader2.forward()
        reader2.peek() shouldBe 'e'.code
        reader2.forward()
        reader2.peek() shouldBe 's'.code
        reader2.forward()
        reader2.peek() shouldBe 't'.code
        reader2.forward()
        reader2.peek() shouldBe 0
    }

    test("peek int") {
        val reader = StreamReader(LoadSettings.builder().build(), "test")
        reader.peek(0) shouldBe 't'.code
        reader.peek(1) shouldBe 'e'.code
        reader.peek(2) shouldBe 's'.code
        reader.peek(3) shouldBe 't'.code
        reader.forward(1)
        reader.peek(0) shouldBe 'e'.code
        reader.peek(1) shouldBe 's'.code
        reader.peek(2) shouldBe 't'.code
    }
})
