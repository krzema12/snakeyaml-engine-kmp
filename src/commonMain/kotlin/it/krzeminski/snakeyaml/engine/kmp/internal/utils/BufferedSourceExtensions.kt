package it.krzeminski.snakeyaml.engine.kmp.internal.utils

import okio.BufferedSource

/**
 * @return a well-formed (if possible) UTF8 string with a size at most [limitBytes]
 */
// based on benchmarks it looks like having inline here actually makes a small difference in the performance
@Suppress("NOTHING_TO_INLINE")
internal inline fun BufferedSource.readUtf8WithLimit(limitBytes: Long): String {
    val fullUtf8Size = sizeOfFullValidUtf8String(limitBytes)
    val buffer = readUtf8(fullUtf8Size)
    return buffer
}

@Suppress("NOTHING_TO_INLINE")
// based on benchmarks it looks like having inline here actually makes a small difference in the performance
private inline fun BufferedSource.sizeOfFullValidUtf8String(limitBytes: Long): Long {
    val hasAll = request(limitBytes)

    val originalSize = if (hasAll) limitBytes else buffer.size
    if (originalSize == 0L) {
        return 0
    }

    val byte = buffer[originalSize - 1]
    if (!isContinuationByte(byte)) {
        // If byte is less than zero we stopped at the start of non-ASCII codepoint
        // we need to shift one byte left in this case.
        // Otherwise, we stopped at ASCII codepoint and we can return the original amount of bytes
        return originalSize - if (byte < 0) 1 else 0
    }

    // Check if the source has one more byte in it
    if (!request(originalSize + 1)) {
        // no more bytes left in the source, return the original size
        return originalSize
    }

    // Check the next byte.
    // If it is not a continuation byte then bytes up to that point form a full "valid" UTF8 string
    // (or at least the last character is complete, and we don't split it in half)
    if (!isContinuationByte(buffer[originalSize])) {
        return originalSize
    }

    var size = originalSize
    // A valid codepoint consists of 1 leading byte and at most 3 continuation bytes (0-3)
    // https://www.rfc-editor.org/rfc/rfc3629.html#section-3
    // We go backwards to find the start of the codepoint at which we have stopped.
    // The marker for that is a non-continuation byte
    repeat(3) {
        size -= 1
        if (size == 0L) {
            // No more data left in the buffer.
            // This means we have an ill-formed UTF8 string.
            // However, the Okio library will handle this and replace invalid codepoints with replacement codepoint
            return originalSize
        }
        val byte = buffer[size - 1]
        if (!isContinuationByte(byte)) {
            // We are on the marker.
            // It might be an ASCII (byte > 0) or Unicode (byte < 0).
            // The first case is probably invalid since we have continuation bytes after ASCII codepoint
            // but this will be handled by Okio.
            // We just need to return the correct position in bytes of the last full codepoint
            return when {
                // ASCII codepoint
                byte >= 0                                -> originalSize
                // this can happen if we start from an ill-formed byte and go over the valid UTF8 codepoint
                // that has less continuation bytes than we read
                continuationBytesCountFor(byte) < it + 1 -> originalSize

                else                                     -> size - 1
            }
        }
    }

    // This can only happen if the edge bytes (n-3,n-2,n-1) are all continuation bytes.
    // That would mean the UTF8 string is ill-formed but, as before, this will be handled by Okio
    return originalSize
}

@Suppress("NOTHING_TO_INLINE")
private inline fun isContinuationByte(byte: Byte): Boolean {
    val asInt = byte.toInt()
    return asInt and 0xc0 == 0x80
}

/**
 * @return the number of bytes that must follow [byte] to form a well-formed UTF8 codepoint
 */
@Suppress("NOTHING_TO_INLINE")
private inline fun continuationBytesCountFor(byte: Byte): Int {
    val asInt = byte.toInt()

    return when {
        // 110xxxxx
        asInt and 0xe0 == 0xc0 -> 1

        // 1110xxxx
        asInt and 0xf0 == 0xe0 -> 2

        // 11110xxx
        asInt and 0xf8 == 0xf0 -> 3

        else -> 0
    }
}
