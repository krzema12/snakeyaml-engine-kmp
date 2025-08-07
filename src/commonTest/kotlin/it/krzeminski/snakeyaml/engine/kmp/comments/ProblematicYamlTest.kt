package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.events.CommentEvent
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.parser.Parser
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

class ProblematicYamlTest : FunSpec({
    val LOAD_OPTIONS: LoadSettings = LoadSettings.builder().setParseComments(true).build()

    test("parse problematic yaml 1") {
        val yamlString1 = "" +
            "key: value\n" +
            "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
            "\n" +
            "  # Comment 2\n"

        val expectedEventIdList = listOf(
            Event.ID.StreamStart,
            Event.ID.DocumentStart,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.Scalar,
            Event.ID.Comment,
            Event.ID.Comment,
            Event.ID.Comment,
            Event.ID.MappingEnd,
            Event.ID.DocumentEnd,
            Event.ID.StreamEnd
        )
        val expectedCommentTypeList = listOf(
            CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK
        )
        val parser = ParserImpl(LOAD_OPTIONS, StreamReader(LOAD_OPTIONS, yamlString1))
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser)
    }

    test("parse problematic yaml 2") {
        val yamlString2 = "" +
            "key: value\n" +
            "\n" +
            "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
            "\n" +
            "  # Comment 2\n"

        val expectedEventIdList = listOf(
            Event.ID.StreamStart,
            Event.ID.DocumentStart,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.Scalar,
            Event.ID.Comment,
            Event.ID.Comment,
            Event.ID.Comment,
            Event.ID.Comment,
            Event.ID.MappingEnd,
            Event.ID.DocumentEnd,
            Event.ID.StreamEnd
        )
        val expectedCommentTypeList = listOf(
            CommentType.BLANK_LINE, CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK
        )
        val parser = ParserImpl(LOAD_OPTIONS, StreamReader(LOAD_OPTIONS, yamlString2))
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser)
    }

    test("parse problematic yaml 3") {
        val yamlString3 = "" +
            "key: value\n" +
            "\n" +
            "key: value\n"

        val expectedEventIdList = listOf(
            Event.ID.StreamStart,
            Event.ID.DocumentStart,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.Scalar,
            Event.ID.Comment,
            Event.ID.Scalar,
            Event.ID.Scalar,
            Event.ID.MappingEnd,
            Event.ID.DocumentEnd,
            Event.ID.StreamEnd
        )
        val expectedCommentTypeList = listOf(CommentType.BLANK_LINE)
        val parser = ParserImpl(LOAD_OPTIONS, StreamReader(LOAD_OPTIONS, yamlString3))
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser)
    }

    test("parse problematic yaml 4") {
        val yamlString4 = "" +
            "---\n" +
            "in the block context:\n" +
            "    indentation should be kept: { \n" +
            "    but in the flow context: [\n" +
            "it may be violated]\n" +
            "}\n" +
            "---\n" +
            "the parser does not require scalars\n" +
            "to be indented with at least one space\n" +
            "...\n" +
            "---\n" +
            "\"the parser does not require scalars\n" +
            "to be indented with at least one space\"\n" +
            "---\n" +
            "foo:\n" +
            "    bar: 'quoted scalars\n" +
            "may not adhere indentation'\n"

        val expectedEventIdList = listOf(
            Event.ID.StreamStart,
            Event.ID.DocumentStart,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.SequenceStart,
            Event.ID.Scalar,
            Event.ID.SequenceEnd,
            Event.ID.MappingEnd,
            Event.ID.MappingEnd,
            Event.ID.MappingEnd,
            Event.ID.DocumentEnd,
            Event.ID.DocumentStart,
            Event.ID.Scalar,
            Event.ID.DocumentEnd,
            Event.ID.DocumentStart,
            Event.ID.Scalar,
            Event.ID.DocumentEnd,
            Event.ID.DocumentStart,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.MappingStart,
            Event.ID.Scalar,
            Event.ID.Scalar,
            Event.ID.MappingEnd,
            Event.ID.MappingEnd,
            Event.ID.DocumentEnd,
            Event.ID.StreamEnd
        )
        val settings = LoadSettings.Companion.builder().build()
        val parser = ParserImpl(settings, StreamReader(settings, yamlString4))
        assertEventListEquals(expectedEventIdList, emptyList(), parser)
    }
})

private fun assertEventListEquals(
    expectedEventIdList: List<Event.ID>,
    expectedCommentTypeList: List<CommentType>,
    parser: Parser
) {
    val commentTypeIterator = expectedCommentTypeList.iterator()
    for (expectedEventId in expectedEventIdList) {
        parser.checkEvent(expectedEventId)
        val event = parser.next()
        if (expectedCommentTypeList.isNotEmpty() && event.eventId == Event.ID.Comment) {
            (event as CommentEvent).commentType shouldBe commentTypeIterator.next()
        }
        event.eventId shouldBe expectedEventId
    }
}
