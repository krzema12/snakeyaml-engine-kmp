package org.snakeyaml.engine.utils

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
