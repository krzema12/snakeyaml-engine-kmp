package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ReaderException
import okio.Buffer
import okio.BufferedSource
import okio.Source
import okio.buffer
import okio.use

class ReaderStringTest : FunSpec({
    test("check printable") {
        val reader = StreamReader(LoadSettings(), "test")
        reader.peek(4) shouldBe 0
        StreamReader.isPrintable("test") shouldBe true
    }

    test("check non-printable") {
        StreamReader.isPrintable("test\u0005 fail") shouldBe false
        shouldThrow<ReaderException> {
            val reader = StreamReader(LoadSettings(), "test\u0005 fail")
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
                val str = charArrayOf(i.toChar()).concatToString()
                val regularExpressionResult = StreamReader.isPrintable(str)

                var charsArrayResult = true
                try {
                    StreamReader(LoadSettings(), str).peek()
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

    test("high surrogates alone") {
        for (i in 0xD800..0xDBFF) {
            val str = charArrayOf(i.toChar()).concatToString()
            val reader = StreamReader(LoadSettings(), str)
            reader.peek() shouldBe '?'.code
        }
    }

    test("low surrogates alone") {
        for (i in 0xDC00..0xDFFF) {
            val str = charArrayOf(i.toChar()).concatToString()
            val reader = StreamReader(LoadSettings(), str)
            reader.peek() shouldBe '?'.code
        }
    }

    test("forward") {
        val reader = StreamReader(LoadSettings(), "test")
        while (reader.peek() != 0) {
            reader.forward(1)
        }
        val reader2 = StreamReader(LoadSettings(), "test")
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
        val reader = StreamReader(LoadSettings(), "test")
        reader.peek(0) shouldBe 't'.code
        reader.peek(1) shouldBe 'e'.code
        reader.peek(2) shouldBe 's'.code
        reader.peek(3) shouldBe 't'.code
        reader.forward(1)
        reader.peek(0) shouldBe 'e'.code
        reader.peek(1) shouldBe 's'.code
        reader.peek(2) shouldBe 't'.code
    }

    test("read first codepoint from a yaml that does not fit in memory") {
        LargeSource(maxSizeBytes = 50.GiB).use { source ->
            val reader = StreamReader(LoadSettings(), source)
            reader.peek() shouldBe '-'.code
        }
    }
})

private val Int.GiB: Long
    get() =  toLong() * 1024 * 1024 * 1024


private class LargeSource(
    private val maxSizeBytes: Long,
    private val buffer: Buffer = Buffer(),
) : Source by buffer {
    var totalBytes: Long = 0
        private set

    private var lines: Long = 0

    private fun initialLine(): String =
        """
        ---
        map:
        - key: "TestValue1"

        """.trimIndent()

    private fun followingLine(): String =
        """
        - key${lines++}: "TestValue${lines}"

        """.trimIndent()

    override fun read(sink: Buffer, byteCount: Long): Long {
        fillBuffer(byteCount)
        return buffer.read(sink, byteCount)
    }

    private fun fillBuffer(bytes: Long) {
        if (bytes == 0L) {
            return
        }

        if (totalBytes >= maxSizeBytes) {
            return
        }

        val initSize = buffer.size

        if (totalBytes == 0L) {
            buffer.writeUtf8(initialLine())
        }
        do {
            buffer.writeUtf8(followingLine())
            if (buffer.size >= maxSizeBytes) {
                break
            }
        } while (buffer.size < bytes)

        totalBytes += buffer.size - initSize
    }
}
