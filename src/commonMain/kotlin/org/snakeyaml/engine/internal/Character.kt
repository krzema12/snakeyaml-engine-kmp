package org.snakeyaml.engine.internal

import kotlin.Char.Companion.MIN_HIGH_SURROGATE
import kotlin.Char.Companion.MIN_LOW_SURROGATE

/**
 * Kotlin Multiplatform equivalent for `java.lang.Character`
 */
internal object Character {

    /**
     * See https://www.tutorialspoint.com/java/lang/character_issupplementarycodepoint.htm
     *
     * Determines whether the specified character (Unicode code point) is in the supplementary character range.
     * The supplementary character range in the Unicode system falls in `U+10000` to `U+10FFFF`.
     *
     * The Unicode code points are divided into two categories:
     * Basic Multilingual Plane (BMP) code points and Supplementary code points.
     * BMP code points are present in the range U+0000 to U+FFFF.
     *
     * Whereas, supplementary characters are rare characters that are not represented using the original 16-bit Unicode.
     * For example, these type of characters are used in Chinese or Japanese scripts and hence, are required by the
     * applications used in these countries.
     *
     * @returns `true` if the specified code point falls in the range of supplementary code points
     * ([MIN_SUPPLEMENTARY_CODE_POINT] to [MAX_CODE_POINT], inclusive), `false` otherwise.
     */
    internal fun isSupplementaryCodePoint(codePoint: Int): Boolean =
        codePoint in MIN_SUPPLEMENTARY_CODE_POINT..MAX_CODE_POINT

    internal fun charCount(codePoint: Int): Int = if (codePoint <= MIN_SUPPLEMENTARY_CODE_POINT) 1 else 2

    internal fun isValidCodePoint(codePoint: Int): Boolean = codePoint in MIN_CODE_POINT..MAX_CODE_POINT

    internal fun isSurrogatePair(highSurrogate: Char, lowSurrogate: Char): Boolean =
        highSurrogate.isHighSurrogate() && lowSurrogate.isLowSurrogate()

    internal fun toCodePoint(highSurrogate: Char, lowSurrogate: Char): Int =
        (highSurrogate.code shl 10) + lowSurrogate.code + SURROGATE_DECODE_OFFSET

    internal fun toChars(codePoint: Int): CharArray = when {
        isBmpCodePoint(codePoint) -> charArrayOf(codePoint.toChar())
        else                      -> charArrayOf(highSurrogateOf(codePoint), lowSurrogateOf(codePoint))
    }

//    fun toChars(
//        codePoint: Int,
//        destination: CharArray,
//        offset: Int,
//    ): Int {
//        return if (isBmpCodePoint(codePoint)) {
//            destination[offset] = codePoint.toChar()
//            1
//        } else {
//            // When writing the low surrogate succeeds but writing the high surrogate fails (offset = -1), the
//            // destination will be modified even though the method throws. This feels wrong, but matches the behavior
//            // of the Java stdlib implementation.
//            destination[offset + 1] = lowSurrogateOf(codePoint)
//            destination[offset] = highSurrogateOf(codePoint)
//            2
//        }
//    }
    /** Basic Multilingual Plane (BMP) */
    internal fun isBmpCodePoint(codePoint: Int): Boolean = codePoint ushr 16 == 0

    internal fun highSurrogateOf(codePoint: Int): Char =
        ((codePoint ushr 10) + HIGH_SURROGATE_ENCODE_OFFSET.code).toChar()

    internal fun lowSurrogateOf(codePoint: Int): Char =
        ((codePoint and 0x3FF) + MIN_LOW_SURROGATE.code).toChar()

//    private fun CharArray.setSafe(index: Int, value: Char) {
//        if (index !in this.indices) {
//            throw IndexOutOfBoundsException("Size: $size, offset: $index")
//        }
//
//        this[index] = value
//    }

    private const val MIN_CODE_POINT: Int = 0x000000
    private const val MAX_CODE_POINT: Int = 0x10FFFF

    private const val MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

    private const val SURROGATE_DECODE_OFFSET: Int =
        MIN_SUPPLEMENTARY_CODE_POINT -
            (MIN_HIGH_SURROGATE.code shl 10) -
            MIN_LOW_SURROGATE.code

    private const val HIGH_SURROGATE_ENCODE_OFFSET: Char = MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10)

}
