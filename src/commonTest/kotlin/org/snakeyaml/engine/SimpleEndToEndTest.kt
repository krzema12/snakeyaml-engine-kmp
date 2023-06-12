package org.snakeyaml.engine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.*

class SimpleEndToEndTest : FunSpec({
    test("simple case") {
        // Given
        val settings = DumpSettings.builder()
            .build()
        val stringBuilder = StringBuilder()
        val writer = object : StreamDataWriter {
            override fun flush() {
                // no-op
            }

            override fun write(str: String) {
                stringBuilder.append(str)
            }

            override fun write(str: String, off: Int, len: Int) {
                stringBuilder.append(str)
            }
        }
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
        stringBuilder.toString() shouldBe """
            foo: bar
            baz: goo

        """.trimIndent()
    }
})
