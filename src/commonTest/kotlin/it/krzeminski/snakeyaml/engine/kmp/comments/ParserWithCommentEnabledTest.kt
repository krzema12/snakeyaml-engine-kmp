package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.assertions.AssertionErrorBuilder.Companion.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.Event.ID
import it.krzeminski.snakeyaml.engine.kmp.parser.Parser
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

class ParserWithCommentEnabledTest : FunSpec({

    test("empty") {
        val expectedEventIdList = listOf(ID.StreamStart, ID.StreamEnd)
        val data = ""
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("parse with only comment") {
        val data = "# Comment"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("comment ending a line") {
        val data = "" +
            "key: # Comment\n" +
            "    value\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Scalar,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("multiline comment") {
        val data = "" +
            "key: # Comment\n" +
            "         # lines\n" +
            "    value\n" +
            "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Comment, ID.Scalar,
            ID.Comment,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("blank line") {
        val data = "" +
            "\n"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("blank line comments") {
        val data = "" +
            "\n" +
            "abc: def # commment\n" +
            "\n" +
            "\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Scalar, ID.Comment,
            ID.Comment,
            ID.Comment,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("block scalar") {
        val data = "" +
            "abc: > # Comment\n" +
            "        def\n" +
            "        hij\n" +
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
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("directive line end comment") {
        val data = "%YAML 1.1 #Comment\n---"
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.DocumentStart,
            ID.Scalar,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("sequence") {
        val data = "" +
            "# Comment\n" +
            "list: # InlineComment1\n" +
            "# Block Comment\n" +
            "- item # InlineComment2\n" +
            "# Comment\n"

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Comment,
            ID.SequenceStart,
            ID.Scalar, ID.Comment,
            ID.Comment,
            ID.SequenceEnd,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("all comments 1") {
        val data = "" +
            "# Block Comment1\n" +
            "# Block Comment2\n" +
            "key: # Inline Comment1a\n" +
            "         # Inline Comment1b\n" +
            "    # Block Comment3a\n" +
            "    # Block Comment3b\n" +
            "    value # Inline Comment2\n" +
            "# Block Comment4\n" +
            "list: # InlineComment3a\n" +
            "            # InlineComment3b\n" +
            "# Block Comment5\n" +
            "- item1 # InlineComment4\n" +
            "- item2: [ value2a, value2b ] # InlineComment5\n" +
            "- item3: { key3a: [ value3a1, value3a2 ], key3b: value3b } # InlineComment6\n" +
            "# Block Comment6\n" +
            "---\n" +
            "# Block Comment7\n" +
            ""

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.Comment,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar, ID.Comment, ID.Comment,

            ID.Comment, ID.Comment,
            ID.Scalar, ID.Comment,

            ID.Comment,
            ID.Scalar, ID.Comment, ID.Comment,
            ID.Comment,

            ID.SequenceStart,
            ID.Scalar, ID.Comment,
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
            ID.Comment,
            ID.MappingEnd,
            ID.SequenceEnd,
            ID.MappingEnd, ID.DocumentEnd,

            ID.DocumentStart,
            ID.Comment,
            ID.Scalar, // Empty
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("all comments 2") {
        val data = "" +
            "# Block Comment1\n" +
            "# Block Comment2\n" +
            "- item1 # Inline Comment1a\n" +
            "                # Inline Comment1b\n" +
            "# Block Comment3a\n" +
            "# Block Comment3b\n" +
            "- item2: value # Inline Comment2\n" +
            "# Block Comment4\n" +
            ""

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.Comment,
            ID.DocumentStart,
            ID.SequenceStart,
            ID.Scalar, ID.Comment, ID.Comment,
            ID.Comment,
            ID.Comment,
            ID.MappingStart,
            ID.Scalar, ID.Scalar, ID.Comment,
            ID.Comment,
            ID.MappingEnd,
            ID.SequenceEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("all comments 3") {
        val data = "" +
            "# Block Comment1\n" +
            "[ item1, item2: value2, {item3: value3} ] # Inline Comment1\n" +
            "# Block Comment2\n" +
            ""
        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.DocumentStart,
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
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }

    test("keeping new line inside sequence") {
        val data = "" + "\n" + "key:\n" + "\n" + "- item1\n" + "\n" + // Per Spec this is part of
            // plain scalar above
            "- item2\n" + "\n" + // Per Spec this is part of plain scalar above
            "- item3\n" + "\n" + // Should be comment?
            "key2: value2\n" + "\n" + // Should be comment?
            "key3: value3\n" + "\n" + // Should be comment?
            ""

        val expectedEventIdList = listOf(
            ID.StreamStart,
            ID.Comment,
            ID.DocumentStart,
            ID.MappingStart,
            ID.Scalar,
            ID.Comment,
            ID.SequenceStart,
            ID.Scalar,
            ID.Scalar,
            ID.Scalar,
            ID.Comment,
            ID.SequenceEnd,
            ID.Scalar,
            ID.Scalar,
            ID.Comment,
            ID.Scalar,
            ID.Scalar,
            ID.Comment,
            ID.MappingEnd,
            ID.DocumentEnd,
            ID.StreamEnd
        )
        val sut = createParser(data)
        assertEventListEquals(expectedEventIdList, sut)
    }
})

private fun assertEventListEquals(expectedEventIdList: List<ID>, parser: Parser) {
    for (expectedEventId in expectedEventIdList) {
        val eventExists = parser.checkEvent(expectedEventId)
        val event = parser.next()
        if (!eventExists) {
            fail("Missing event: $expectedEventId")
        }
        event.eventId shouldBe expectedEventId
    }
}

private fun createParser(data: String): Parser {
    val loadSettings = LoadSettings(parseComments = true)
    return ParserImpl(loadSettings, StreamReader(loadSettings, data))
}
