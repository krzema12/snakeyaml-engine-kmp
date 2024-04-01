package org.snakeyaml.engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.DocumentEndEvent
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.ImplicitTuple
import org.snakeyaml.engine.v2.events.MappingEndEvent
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.StreamEndEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import java.io.StringWriter

class DumpTest {
    @Test
    fun testWidth() {
        val settings =
            DumpSettings.builder()
                // Otherwise line breaks appear in places that create an incorrect YAML, e.g. in the middle of GitHub
                // expressions.
                .setWidth(Int.MAX_VALUE)
                .build()
        val writer =
            object : StringWriter(), StreamDataWriter {
                override fun flush() {
                    // no-op
                }
            }
        val emitter = Emitter(settings, writer)
        emitter.emit(StreamStartEvent())
        emitter.emit(DocumentStartEvent(false, null, emptyMap()))

        emitter.emit(MappingStartEvent(null, null, true, FlowStyle.BLOCK))
        // key
        emitter.emit(
            ScalarEvent(
                null,
                null,
                ImplicitTuple(true, true),
                "foo",
                ScalarStyle.PLAIN,
            ),
        )
        // value
        emitter.emit(
            ScalarEvent(null, null, ImplicitTuple(true, true), "000 111 222 333 444 555 666 777 888 999 000 111 222 333 444 555 666 777 888 999 000 111 222 333 444 555 666 777 888 999", ScalarStyle.SINGLE_QUOTED),
        )
        emitter.emit(MappingEndEvent())

        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())
        val asString = writer.toString()
        assertEquals(
            "foo: '000 111 222 333 444 555 666 777 888 999 000 111 222 333 444 555 666 777 888 999 000 111 222 333 444 555 666 777 888 999'\n",
            asString,
        )
    }
}
