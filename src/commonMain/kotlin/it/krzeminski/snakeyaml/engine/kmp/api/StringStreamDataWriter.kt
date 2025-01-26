package it.krzeminski.snakeyaml.engine.kmp.api

import okio.Buffer

/** Internal helper class to support dumping to [String] */
internal class StringStreamDataWriter(
    private val buffer: Buffer = Buffer(),
) : StreamDataWriter {
    override fun flush(): Unit = buffer.flush()

    override fun write(str: String) {
        buffer.writeUtf8(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        buffer.writeUtf8(string = str, beginIndex = off, endIndex = off + len)
    }

    /**
     * Returns the contents of the internal buffer, without altering the buffer's state.
     */
    override fun toString(): String = buffer.peek().readUtf8()
}
