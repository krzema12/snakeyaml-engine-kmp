package it.krzeminski.snakeyaml.engine.kmp.issues.issue36

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.*

private class StreamToStringWriter : StreamDataWriter {
    private val content = StringBuilder()

    override fun write(str: String) {
        content.append(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        content.append(str.substring(off, off + len))
    }

    override fun flush() {
        // No-op
    }

    override fun toString(): String = content.toString()
}

class EmitCommentTest : FunSpec({
    test("Issue 36: comment with scalar should not be ignored") {
        val settings = DumpSettings.builder().setDumpComments(true).build()
        val writer = StreamToStringWriter()
        val emitter = Emitter(settings, writer)
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, null, emptyMap()))
        emitter.emit(CommentEvent(CommentType.BLOCK, "Hello world!", null, null))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, true),
            "This is the scalar", ScalarStyle.DOUBLE_QUOTED))
        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())

        writer.toString() shouldBe "#Hello world!\n\"This is the scalar\"\n"
    }

    test("Issue 36: only comment should not be ignored") {
        val settings = DumpSettings.builder().setDumpComments(true).build()
        val writer = StreamToStringWriter()
        val emitter = Emitter(settings, writer)
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, null, emptyMap()))
        emitter.emit(CommentEvent(CommentType.BLOCK, "Hello world!", null, null))
        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())

        writer.toString() shouldBe "#Hello world!\n"
    }
})
