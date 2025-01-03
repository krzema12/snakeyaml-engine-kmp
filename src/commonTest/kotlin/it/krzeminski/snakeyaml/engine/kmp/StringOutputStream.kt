package it.krzeminski.snakeyaml.engine.kmp

import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter

/**
 * A convenient utility used to assert on certain output.
 */
internal class StringOutputStream(
    private val builder: StringBuilder = StringBuilder(),
) : StreamDataWriter {
    override fun write(str: String) {
        builder.append(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        builder.append(str.drop(off).take(len))
    }

    override fun toString(): String = builder.toString()
}
