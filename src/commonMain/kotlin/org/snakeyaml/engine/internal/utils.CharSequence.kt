package org.snakeyaml.engine.internal

// https://github.com/cketti/kotlin-codepoints/blob/4c3929ea8914fe257ed486cb1eb4e14b98ff466e/kotlin-codepoints/src/commonMain/kotlin/CharSequenceExtensions.kt


/** @see CharArray.codePointCount */
internal fun CharSequence.codePointCount(
    beginIndex: Int = 0,
    endIndex: Int = length,
): Int =
    toString().toCharArray().codePointCount(beginIndex = beginIndex, endIndex = endIndex)

/**
 * Returns the number of Unicode code points in the specified text range of this `CharSequence`.
 *
 * The text range begins at the specified `beginIndex` and extends to the `Char` at index `endIndex - 1`. Thus, the
 * length (in `Char`s) of the text range is `endIndex - beginIndex`. Unpaired surrogates within the text range count as
 * one code point each.
 *
 * If `beginIndex` is negative, or `endIndex` is larger than the length of this string, or `beginIndex` is larger than
 * `endIndex`, this method throws an [IndexOutOfBoundsException].
 */
internal fun CharArray.codePointCount(
    beginIndex: Int = 0,
    endIndex: Int = size,
): Int {
    if (beginIndex < 0) throw IndexOutOfBoundsException("beginIndex must not be less than 0, but was $beginIndex")
    if (endIndex > size) throw IndexOutOfBoundsException("endIndex must not be greater than size ($size), but was $endIndex")
    if (beginIndex > endIndex) throw IndexOutOfBoundsException("beginIndex must not be greater than endIndex ($endIndex), but was $beginIndex")

    var index = beginIndex
    var count = 0
    while (index < endIndex) {
        val firstChar = this[index]
        index++
        if (firstChar.isHighSurrogate() && index < endIndex) {
            val nextChar = this[index]
            if (nextChar.isLowSurrogate()) {
                index++
            }
        }

        count++
    }

    return count
}


internal fun CharSequence.toCodePoints(): IntArray {
    val codePoints = IntArray(codePointCount())
    var i = 0
    var c = 0
    while (i < length) {
        val cp = codePointAt(i)
        codePoints[c] = cp
        i += Character.charCount(cp)
        c++
    }
    return codePoints
}


/**
 * Returns the Unicode code point at the specified index.
 *
 * The `index` parameter is the regular `CharSequence` index, i.e. the number of `Char`s from the start of the character
 * sequence.
 *
 * If the code point at the specified index is part of the Basic Multilingual Plane (BMP), its value can be represented
 * using a single `Char` and this method will behave exactly like [CharSequence.get].
 * Code points outside the BMP are encoded using a surrogate pair – a `Char` containing a value in the high surrogate
 * range followed by a `Char` containing a value in the low surrogate range. Together these two `Char`s encode a single
 * code point in one of the supplementary planes. This method will do the necessary decoding and return the value of
 * that single code point.
 *
 * In situations where surrogate characters are encountered that don't form a valid surrogate pair starting at `index`,
 * this method will return the surrogate code point itself, behaving like [CharSequence.get].
 *
 * If the `index` is out of bounds of this character sequence, this method throws an [IndexOutOfBoundsException].
 *
 * To iterate over all code points in a character sequence the index has to be adjusted depending on the value of the
 * returned code point. Use [CodePoints.charCount] for this.
 *
 * ```kotlin
 * // Text containing code points outside the BMP (encoded as a surrogate pairs)
 * val text = "\uD83E\uDD95\uD83E\uDD96"
 *
 * var index = 0
 * while (index < text.length) {
 *     val codePoint = text.codePointAt(index)
 *     // Do something with codePoint
 *
 *     index += CodePoints.charCount(codePoint)
 * }
 * ```
 */
internal fun CharSequence.codePointAt(index: Int): Int {
    if (index !in indices) throw IndexOutOfBoundsException("index $index was not in range $indices")

    val firstChar = this[index]
    if (firstChar.isHighSurrogate()) {
        val nextChar = getOrNull(index + 1)
        if (nextChar?.isLowSurrogate() == true) {
            return Character.toCodePoint(firstChar, nextChar)
        }
    }

    return firstChar.code
}
