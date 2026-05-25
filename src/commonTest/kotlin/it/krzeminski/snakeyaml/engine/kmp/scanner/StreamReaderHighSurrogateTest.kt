package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class StreamReaderHighSurrogateTest : FunSpec({
    test("high surrogate at buffer boundary") {
        val sb = StringBuilder()
        for (i in 0..<1024) {
            sb.append('a')
        }
        sb.append('\uD800')
        sb.append('\uDC00')

        val settings = LoadSettings(bufferSize = 1024)
        val reader = StreamReader(settings, sb.toString())

        var count = 0
        while (reader.peek() != 0) {
            reader.forward(1)
            count++
        }
        withClue("Should read 1025 code points (1024 ASCII + 1 supplementary)") {
            count shouldBe 1025
        }
    }

    test("high surrogate at multiple buffer boundaries") {
        val settings = LoadSettings(bufferSize = 10)
        val input = "123456789\uD800\uDC00abcdefghij\uD801\uDC01xyz"

        val reader = StreamReader(settings, input)

        var count = 0
        while (reader.peek() != 0) {
            reader.forward(1)
            count++
        }
        withClue("Should read 24 code points") {
            count shouldBe 24
        }
    }
})
