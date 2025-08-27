package it.krzeminski.snakeyaml.engine.kmp.issues.issue23

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ImplicitTuple
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent

private class MyWriter : StreamDataWriter {
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

private fun dump(value: String): String {
    val settings = DumpSettings.builder().build()
    val writer = MyWriter()
    val emitter = Emitter(settings, writer)
    emitter.emit(StreamStartEvent())
    emitter.emit(DocumentStartEvent(false, null, emptyMap()))
    emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, false),
        value, ScalarStyle.PLAIN, null, null))
    return writer.toString()
}

class EmptyStringOutputTest : FunSpec({
    test("Output empty string") {
        val dumper = Dump(DumpSettings.builder().build())
        val output = dumper.dumpToString("")
        output shouldBe "''\n"
    }

    test("Output empty string with explicit start") {
        val dumper = Dump(DumpSettings.builder().setExplicitStart(true).build())
        val output = dumper.dumpToString("")
        output shouldBe "--- ''\n"
    }

    test("Output empty string with emitter") {
        dump("") shouldBe "---"
    }

    test("Output string with emitter") {
        dump("v1234512345") shouldBe "v1234512345"
    }
})
