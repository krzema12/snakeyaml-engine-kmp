package org.snakeyaml.engine.v2.representer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.emitter.Emitter;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.serializer.Serializer;

import java.io.StringWriter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("fast")
public class RepresentEntryTest {

    private final DumpSettings settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN).setDefaultFlowStyle(FlowStyle.BLOCK).build();
    private final CommentedEntryRepresenter commentedEntryRepresenter = new CommentedEntryRepresenter(settings);

    private Map<String, String> createMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "val1");
        return map;
    }

    @Test
    @DisplayName("Represent and dump mapping nodes using the new method")
    void representMapping() {
        StringOutputStream stringOutputStream = new StringOutputStream();

        Serializer serializer = new Serializer(settings, new Emitter(settings, stringOutputStream));
        serializer.open();
        serializer.serialize(commentedEntryRepresenter.represent(createMap()));
        serializer.close();

        assertEquals("#Key node block comment\n" +
                "a: val1 #Value node inline comment\n", stringOutputStream.toString());
    }

    private class CommentedEntryRepresenter extends StandardRepresenter {

        public CommentedEntryRepresenter(DumpSettings settings) {
            super(settings);
        }

        @Override
        protected NodeTuple representMappingEntry(Map.Entry<?, ?> entry) {
            NodeTuple tuple = super.representMappingEntry(entry);
            List<CommentLine> keyBlockComments = new ArrayList<>();
            keyBlockComments.add(new CommentLine(Optional.empty(), Optional.empty(), "Key node block comment", CommentType.BLOCK));
            tuple.getKeyNode().setBlockComments(keyBlockComments);

            List<CommentLine> valueEndComments = new ArrayList<>();
            valueEndComments.add(new CommentLine(Optional.empty(), Optional.empty(), "Value node inline comment", CommentType.IN_LINE));
            tuple.getValueNode().setEndComments(valueEndComments);

            return tuple;
        }
    }

    private class StringOutputStream extends StringWriter implements StreamDataWriter {}
}