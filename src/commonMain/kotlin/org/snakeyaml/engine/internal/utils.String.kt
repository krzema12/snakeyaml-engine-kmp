package org.snakeyaml.engine.internal

/**
 * Convert this [String] (from [startIndex] to [endIndex]) into a [CharArray] and copy it into [destination].
 */
internal fun String.toCharArray(
    destination: CharArray,
    destinationOffset: Int,
    startIndex: Int,
    endIndex: Int,
): CharArray = toCharArray(startIndex = startIndex, endIndex = endIndex)
    .copyInto(destination = destination, destinationOffset = destinationOffset)


/**
 * Appends the string representation of the [codePoint] argument to this Appendable and returns this instance.
 *
 * To append the codepoint, [Appendable.append(Char)][Appendable.append] is called [CodePoints.charCount] times.
 *
 * The overall effect is exactly as if the argument were converted to a char array by the function
 * [CodePoints.toChars] and the characters in that array were then appended to this Appendable.
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
