package it.krzeminski.snakeyaml.engine.kmp.issues.issue36

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.*

class EmitCommentTest : FunSpec({
    test("comment with scalar should not be ignored") {
        val settings = DumpSettings(dumpComments = true)
        val writer = StreamToStringWriter()
        val emitter = Emitter(settings, writer)
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()))
        emitter.emit(CommentEvent(
            commentType = CommentType.BLOCK,
            value = "Hello world!",
            startMark = null,
            endMark = null
        ))
        emitter.emit(ScalarEvent(
            anchor = null,
            tag = null,
            implicit = ImplicitTuple(plain = true, nonPlain = true),
            value = "This is the scalar",
            scalarStyle = ScalarStyle.DOUBLE_QUOTED,
        ))
        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())

        writer.toString() shouldBe "#Hello world!\n\"This is the scalar\"\n"
    }

    test("only comment should not be ignored") {
        val settings = DumpSettings(dumpComments = true)
        val writer = StreamToStringWriter()
        val emitter = Emitter(settings, writer)
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()))
        emitter.emit(CommentEvent(
            commentType = CommentType.BLOCK,
            value = "Hello world!",
            startMark = null,
            endMark = null
        ))
        emitter.emit(DocumentEndEvent(isExplicit = false))
        emitter.emit(StreamEndEvent())

        writer.toString() shouldBe "#Hello world!\n"
    }
})

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
