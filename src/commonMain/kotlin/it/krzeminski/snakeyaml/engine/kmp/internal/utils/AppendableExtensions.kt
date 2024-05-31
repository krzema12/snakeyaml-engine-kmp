/*
 * MIT License
 *
 * Copyright (c) 2023 cketti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package it.krzeminski.snakeyaml.engine.kmp.internal.utils

// https://github.com/cketti/kotlin-codepoints/blob/4c3929ea8914fe257ed486cb1eb4e14b98ff466e/kotlin-codepoints/src/commonMain/kotlin/AppendableExtensions.kt

/**
 * Appends the string representation of the [codePoint] argument to this Appendable and returns this instance.
 *
 * To append the codepoint, [Appendable.append(Char)][Appendable.append] is called
 * [Character.charCount] times.
 *
 * The overall effect is exactly as if the argument were converted to a char array by the function
 * [Character.toChars] and the characters in that array were then appended to this Appendable.
 */
internal fun <T : Appendable> T.appendCodePoint(codePoint: Int): Appendable {
    if (Character.isBmpCodePoint(codePoint)) {
        append(codePoint.toChar())
    } else {
        append(Character.highSurrogateOf(codePoint))
        append(Character.lowSurrogateOf(codePoint))
    }
    return this
}

internal fun Iterable<Int>.joinCodepointsToString(): String =
    buildString {
        appendCodePoints(this@joinCodepointsToString)
    }

private fun <T : Appendable> T.appendCodePoints(codePoints: Iterable<Int>): T {
    codePoints.forEach { appendCodePoint(it) }
    return this
}
