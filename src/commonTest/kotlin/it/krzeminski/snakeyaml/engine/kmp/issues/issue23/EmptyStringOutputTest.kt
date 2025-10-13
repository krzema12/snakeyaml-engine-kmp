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

class EmptyStringOutputTest : FunSpec({
    test("output empty string") {
        val dumper = Dump(DumpSettings())
        val output = dumper.dumpToString("")
        output shouldBe "''\n"
    }

    test("output empty string with explicit start") {
        val dumper = Dump(DumpSettings(isExplicitStart = true))
        val output = dumper.dumpToString("")
        output shouldBe "--- ''\n"
    }

    test("output empty string with emitter") {
        dump("") shouldBe "---"
    }

    test("output string with emitter") {
        dump("v1234512345") shouldBe "v1234512345"
    }
})

private fun dump(value: String): String {
    val settings = DumpSettings()
    val writer = MyWriter()
    val emitter = Emitter(settings, writer)
    emitter.emit(StreamStartEvent())
    emitter.emit(DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()))
    emitter.emit(ScalarEvent(
        anchor = null,
        tag = null,
        implicit = ImplicitTuple(plain = true, nonPlain = false),
        value = value,
        scalarStyle = ScalarStyle.PLAIN,
        startMark = null,
        endMark = null,
    ))
    return writer.toString()
}

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
