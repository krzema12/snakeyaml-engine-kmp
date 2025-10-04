package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitable
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.events.Event.ID
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import it.krzeminski.snakeyaml.engine.kmp.serializer.Serializer

class SerializerWithCommentEnabledTest: FunSpec({
    test("empty") {
        val expectedEventIdList = listOf(ID.StreamStart, ID.StreamEnd)
        val data = ""

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("parse with only comment") {
        val data = "# Comment"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd,
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("comment ending a line") {
        val data = "key: # Comment\n" +
            "  value\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Scalar,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd,
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("multi-line comment") {
        val data = "key: # Comment\n" +
            "     # lines\n" +
            "  value\n" +
            "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Comment, ID.Scalar,
            ID.MappingEnd,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("blank line") {
        val data = "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd,
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("blank line comments") {
        val data = "\n" +
            "abc: def # comment\n" +
            "\n" +
            "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Comment,
            ID.Scalar, ID.Scalar, ID.Comment,
            ID.MappingEnd,
            ID.Comment,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("block scalar") {
        val data = "abc: > # Comment\n" +
            "    def\n" +
            "    hij\n" +
            "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment,
            ID.Scalar,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("directive line end comment") {
        val data ="%YAML 1.1 #Comment\n---"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.Scalar,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("sequence") {
        val data = "# Comment\n" +
            "list: # InlineComment1\n" +
            "# Block Comment\n" +
            "- item # InlineComment2\n" +
            "# Comment\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Comment,
            ID.Scalar, ID.Comment,
            ID.SequenceStart,
            ID.Comment,
            ID.Scalar, ID.Comment,
            ID.SequenceEnd,
            ID.MappingEnd,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("all comments 1") {
        val data = "# Block Comment1\n" +
        "# Block Comment2\n" +
            "key: # Inline Comment1a\n" +
            "     # Inline Comment1b\n" +
            "  # Block Comment3a\n" +
            "  # Block Comment3b\n" +
            "  value # Inline Comment2\n" +
            "# Block Comment4\n" +
            "list: # InlineComment3a\n" +
            "      # InlineComment3b\n" +
            "# Block Comment5\n" +
            "- item1 # InlineComment4\n" +
            "- item2: [ value2a, value2b ] # InlineComment5\n" +
            "- item3: { key3a: [ value3a1, value3a2 ], key3b: value3b } # InlineComment6\n" +
            "# Block Comment6\n" +
            "---\n" +
            "# Block Comment7\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Comment,
            ID.Comment,
            ID.Scalar, ID.Comment, ID.Comment,

            ID.Comment, ID.Comment,
            ID.Scalar, ID.Comment,

            ID.Comment,
            ID.Scalar, ID.Comment, ID.Comment,

            ID.SequenceStart,
            ID.Comment,
            ID.Scalar,
            ID.Comment,

            ID.MappingStart,
            ID.Scalar, ID.SequenceStart, ID.Scalar, ID.Scalar, ID.SequenceEnd, ID.Comment,
            ID.MappingEnd,

            ID.MappingStart,
            ID.Scalar, // value=item3
            ID.MappingStart,
            ID.Scalar, // value=key3a
            ID.SequenceStart,
            ID.Scalar, // value=value3a
            ID.Scalar, // value=value3a2
            ID.SequenceEnd,
            ID.Scalar, // value=key3b
            ID.Scalar, // value=value3b
            ID.MappingEnd,
            ID.Comment, // type=IN_LINE, value= InlineComment6
            ID.MappingEnd,
            ID.SequenceEnd,
            ID.MappingEnd,
            ID.Comment,
            ID.DocumentEnd,

            ID.DocumentStart,
            ID.Comment,
            ID.Scalar, // Empty
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("all comments 2") {
        val data = "# Block Comment1\n" +
        "# Block Comment2\n" +
            "- item1 # Inline Comment1a\n" +
            "        # Inline Comment1b\n" +
            "# Block Comment3a\n" +
            "# Block Comment3b\n" +
            "- item2: value # Inline Comment2\n" +
            "# Block Comment4\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.SequenceStart,
            ID.Comment,
            ID.Comment,
            ID.Scalar, ID.Comment, ID.Comment,
            ID.MappingStart,
            ID.Comment,
            ID.Comment,
            ID.Scalar, ID.Scalar, ID.Comment,
            ID.MappingEnd,
            ID.SequenceEnd,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }

    test("all comments 3") {
        val data = "# Block Comment1\n" +
        "[ item1, item2: value2, {item3: value3} ] # Inline Comment1\n" +
            "# Block Comment2\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.Comment,
            ID.SequenceStart,
            ID.Scalar, ID.MappingStart,
            ID.Scalar, ID.Scalar,
            ID.MappingEnd,
            ID.MappingStart,
            ID.Scalar, ID.Scalar,
            ID.MappingEnd,
            ID.SequenceEnd,
            ID.Comment,
            ID.Comment,
            ID.DocumentEnd,
            ID.StreamEnd
        )

        val result = serializeWithCommentsEnabled(data)

        assertEventListEquals(expectedEventIdList, result)
    }
})

private fun serializeWithCommentsEnabled(data: String): List<Event> {
    val emitter = TestEmitter()
    val dumpSettings = DumpSettings.builder()
        .setDefaultScalarStyle(ScalarStyle.PLAIN)
        .setDumpComments(true)
        .setDefaultFlowStyle(FlowStyle.BLOCK)
        .build()
    val serializer = Serializer(dumpSettings, emitter)
    serializer.emitStreamStart()
    val settings = LoadSettings(parseComments = true)
    val composer = Composer(settings, ParserImpl(settings, StreamReader(settings, data)))
    while (composer.hasNext()) {
        serializer.serializeDocument(composer.next())
    }
    serializer.emitStreamEnd()
    return emitter.getEventList()
}

private fun assertEventListEquals(expectedEventIdList: List<ID>, actualEvents: List<Event>) {
    expectedEventIdList shouldBe actualEvents.map { it.eventId }
}

private class TestEmitter : Emitable {
    private val eventList = mutableListOf<Event>()

    override fun emit(event: Event) {
        eventList.add(event)
    }

    fun getEventList(): List<Event> = eventList
}
