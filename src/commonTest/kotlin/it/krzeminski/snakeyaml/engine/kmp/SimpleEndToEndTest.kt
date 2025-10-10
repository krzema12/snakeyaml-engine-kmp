package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StringStreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.events.*

class SimpleEndToEndTest : FunSpec({
    test("simple case") {
        // Given
        val settings = DumpSettings()
        val writer = StringStreamDataWriter()
        val emitter = Emitter(opts = settings, stream = writer)

        // When
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, null, emptyMap()))

        emitter.emit(MappingStartEvent(null, null, true, FlowStyle.BLOCK))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, true), "foo", ScalarStyle.PLAIN))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, true), "bar", ScalarStyle.PLAIN))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, true), "baz", ScalarStyle.PLAIN))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, true), "goo", ScalarStyle.PLAIN))
        emitter.emit(MappingEndEvent())

        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())

        // Then
        writer.toString() shouldBe """
            foo: bar
            baz: goo

        """.trimIndent()
    }
})
