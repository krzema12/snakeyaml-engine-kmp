package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

/**
 * Tests that the StreamReader correctly handles high surrogates at buffer boundaries and does not
 * throw IndexOutOfBoundsException or misinterpret surrogate pairs.
 */
class StreamReaderHighSurrogateTest : FunSpec({
    /**
     * Test for IndexOutOfBoundsException when buffer is exactly filled with 1025 code points and the
     * last code point is a supplementary character represented as a surrogate pair.
     * This reproduces the bug where `buffer[1025]` is accessed on a buffer with length 1025 in the
     * upstream Java code (where the buffer was sized for chars, not code points).
     *
     * In the Kotlin version, the analogous bug was an off-by-one in [Character.charCount] where
     * `charCount(0x10000)` returned 1 instead of 2, causing the low surrogate to be processed as a
     * standalone code point and rejected by the printable check.
     */
    test("high surrogate at buffer boundary") {
        // Create a string that is exactly 1024 regular code points + 1 supplementary code point
        // represented as a surrogate pair (2 Java chars = 1 Kotlin code point)
        val sb = StringBuilder()

        // Fill with 1024 regular ASCII characters
        for (i in 0..<1024) {
            sb.append('a')
        }

        // Add a supplementary code point as a surrogate pair
        sb.append('\uD800') // High surrogate
        sb.append('\uDC00') // Low surrogate

        // This should not throw
        val settings = LoadSettings(bufferSize = 1024)
        val reader = StreamReader(settings, sb.toString())

        // Read all code points - this would trigger the bug before the fix
        var count = 0
        while (reader.peek() != 0) {
            reader.forward(1)
            count++
        }

        // We should successfully read all 1025 code points (1024 ASCII + 1 supplementary)
        withClue("Should read 1025 code points (1024 ASCII + 1 supplementary)") {
            count shouldBe 1025
        }
    }

    /**
     * Test with multiple buffer fills where the boundary character is a high surrogate.
     * Ensures surrogate pairs are correctly handled across multiple buffer read cycles.
     */
    test("high surrogate at multiple buffer boundaries") {
        val settings = LoadSettings(bufferSize = 10)
        // Create a string where the 10th character triggers a buffer boundary mid-surrogate-pair
        val input = "123456789\uD800\uDC00abcdefghij\uD801\uDC01xyz"

        val reader = StreamReader(settings, input)

        var count = 0
        while (reader.peek() != 0) {
            reader.forward(1)
            count++
        }

        // Count should match the number of code points
        // 9 regular + 1 surrogate pair + 10 regular + 1 surrogate pair + 3 regular = 24 code points
        withClue("Should read 24 code points") {
            count shouldBe 24
        }
    }
})
