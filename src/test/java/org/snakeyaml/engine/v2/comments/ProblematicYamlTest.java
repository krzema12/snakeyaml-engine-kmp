/*
 * Copyright (c) 2018, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.comments;

import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.Event.ID;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ProblematicYamlTest {
    private final boolean DEBUG = false;

    private void println(String s) {
        if (DEBUG) System.out.println(s);
    }

    private void println() {
        if (DEBUG) System.out.println();
    }

    private static final LoadSettings LOAD_OPTIONS = LoadSettings.builder().setParseComments(true).build();

    private void assertEventListEquals(List<Event.ID> expectedEventIdList, List<CommentType> expectedCommentTypeList,
                                       Parser parser) {
        Iterator<CommentType> commentTypeIterator = expectedCommentTypeList.iterator();
        for (Event.ID expectedEventId : expectedEventIdList) {
            parser.checkEvent(expectedEventId);
            Event event = parser.next();
            println("Expected: " + expectedEventId);
            if (event == null) {
                fail("Missing event: " + expectedEventId);
            }
            println("Got: " + event
                    + (event.getEventId() == Event.ID.Comment ? " " + ((CommentEvent) event).getCommentType() : ""));
            println();
            if (expectedCommentTypeList != null && event.getEventId() == Event.ID.Comment) {
                assertEquals(commentTypeIterator.next(), ((CommentEvent) event).getCommentType());
            }
            assertEquals(expectedEventId, event.getEventId());
        }
    }

    @SuppressWarnings("unused")
    private void printEventList(Parser parser) {
        for (Event event = parser.next(); event != null; event = parser.next()) {
            println("Got: " + event
                    + (event.getEventId() == Event.ID.Comment ? " " + ((CommentEvent) event).getCommentType() : ""));
            println();
        }
    }

    @Test
    public void testParseProblematicYaml1() {
        final String yamlString1 = "" + //
                "key: value\n" + //
                "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
                "\n" + //
                "  # Comment 2\n" + //
                "";

        List<ID> expectedEventIdList = Arrays.asList(//
                ID.StreamStart, //
                ID.DocumentStart, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.Scalar, //
                ID.Comment, //
                ID.Comment, //
                ID.Comment, //
                ID.MappingEnd, //
                ID.DocumentEnd, //
                ID.StreamEnd //
        );
        List<CommentType> expectedCommentTypeList = Arrays.asList(//
                CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK);
        ParserImpl parser = new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString1)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    public void testParseProblematicYaml2() {
        final String yamlString2 = "" + //
                "key: value\n" + //
                "\n" + //
                "  # Comment 1\n" + // s.b BLOCK, classified as INLINE
                "\n" + //
                "  # Comment 2\n" + //
                "";
        List<ID> expectedEventIdList = Arrays.asList(//
                ID.StreamStart, //
                ID.DocumentStart, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.Scalar, //
                ID.Comment, //
                ID.Comment, //
                ID.Comment, //
                ID.Comment, //
                ID.MappingEnd, //
                ID.DocumentEnd, //
                ID.StreamEnd //
        );
        List<CommentType> expectedCommentTypeList = Arrays.asList(//
                CommentType.BLANK_LINE, CommentType.BLOCK, CommentType.BLANK_LINE, CommentType.BLOCK);
        ParserImpl parser = new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString2)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    public void testParseProblematicYaml3() {
        final String yamlString3 = "" + //
                "key: value\n" + //
                "\n" + //
                "key: value\n" + //
                "";
        List<ID> expectedEventIdList = Arrays.asList(//
                ID.StreamStart, //
                ID.DocumentStart, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.Scalar, //
                ID.Comment, //
                ID.Scalar, //
                ID.Scalar, //
                ID.MappingEnd, //
                ID.DocumentEnd, //
                ID.StreamEnd //
        );
        List<CommentType> expectedCommentTypeList = Arrays.asList(//
                CommentType.BLANK_LINE);
        ParserImpl parser = new ParserImpl(LOAD_OPTIONS, new StreamReader(LOAD_OPTIONS, new StringReader(yamlString3)));
        assertEventListEquals(expectedEventIdList, expectedCommentTypeList, parser);
    }

    @Test
    public void testParseProblematicYaml4() {
        String yamlString4 = "" + //
                "---\n" + //
                "in the block context:\n" + //
                "    indentation should be kept: { \n" + //
                "    but in the flow context: [\n" + //
                "it may be violated]\n" + //
                "}\n" + //
                "---\n" + //
                "the parser does not require scalars\n" + //
                "to be indented with at least one space\n" + //
                "...\n" + //
                "---\n" + //
                "\"the parser does not require scalars\n" + //
                "to be indented with at least one space\"\n" + //
                "---\n" + //
                "foo:\n" + //
                "    bar: 'quoted scalars\n" + //
                "may not adhere indentation'\n" + //
                "";
        List<ID> expectedEventIdList = Arrays.asList(//
                ID.StreamStart, //
                ID.DocumentStart, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.SequenceStart, //
                ID.Scalar, //
                ID.SequenceEnd, //
                ID.MappingEnd, //
                ID.MappingEnd, //
                ID.MappingEnd, //
                ID.DocumentEnd, //
                ID.DocumentStart, //
                ID.Scalar, //
                ID.DocumentEnd, //
                ID.DocumentStart, //
                ID.Scalar, //
                ID.DocumentEnd, //
                ID.DocumentStart, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.MappingStart, //
                ID.Scalar, //
                ID.Scalar, //
                ID.MappingEnd, //
                ID.MappingEnd, //
                ID.DocumentEnd, //
                ID.StreamEnd//
        );
        LoadSettings settings = LoadSettings.builder().build();
        Parser parser = new ParserImpl(settings, new StreamReader(settings, new StringReader(yamlString4)));
        assertEventListEquals(expectedEventIdList, new ArrayList<CommentType>(), parser);
    }
}
