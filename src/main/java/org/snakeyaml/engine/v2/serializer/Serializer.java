/*
 * Copyright (c) 2018, SnakeYAML
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
package org.snakeyaml.engine.v2.serializer;

import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.common.Anchor;
import org.snakeyaml.engine.v2.emitter.Emitable;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.CommentEvent;
import org.snakeyaml.engine.v2.events.DocumentEndEvent;
import org.snakeyaml.engine.v2.events.DocumentStartEvent;
import org.snakeyaml.engine.v2.events.ImplicitTuple;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.events.StreamEndEvent;
import org.snakeyaml.engine.v2.events.StreamStartEvent;
import org.snakeyaml.engine.v2.nodes.AnchorNode;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.NodeType;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Serializer {
    private final DumpSettings settings;
    private final Emitable emitable;
    private final Set<Node> serializedNodes;
    private final Map<Node, Anchor> anchors;

    public Serializer(DumpSettings settings, Emitable emitable) {
        this.settings = settings;
        this.emitable = emitable;
        this.serializedNodes = new HashSet();
        this.anchors = new HashMap();
    }

    public void serialize(Node node) {
        this.emitable.emit(new DocumentStartEvent(settings.isExplicitStart(), settings.getYamlDirective(), settings.getTagDirective()));
        anchorNode(node);
        settings.getExplicitRootTag().ifPresent(node::setTag);
        serializeNode(node);
        this.emitable.emit(new DocumentEndEvent(settings.isExplicitEnd()));
        this.serializedNodes.clear();
        this.anchors.clear();
    }

    public void open() {
        this.emitable.emit(new StreamStartEvent());
    }

    public void close() {
        this.emitable.emit(new StreamEndEvent());
        // clean up the resources
        this.anchors.clear();
        this.serializedNodes.clear();
    }

    private void anchorNode(Node node) {
        final Node realNode;
        if (node.getNodeType() == NodeType.ANCHOR) {
            realNode = ((AnchorNode) node).getRealNode();
        } else {
            realNode = node;
        }
        if (this.anchors.containsKey(realNode)) {
            //it looks weird, anchors does contain the key node but we call computeIfAbsent()
            // this is because the value is null (HashMap permits values to be null)
            this.anchors.computeIfAbsent(realNode, a -> settings.getAnchorGenerator().nextAnchor(realNode));
        } else {
            this.anchors.put(realNode, realNode.getAnchor().isPresent() ? settings.getAnchorGenerator().nextAnchor(realNode) : null);
            switch (realNode.getNodeType()) {
                case SEQUENCE:
                    SequenceNode seqNode = (SequenceNode) realNode;
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        anchorNode(item);
                    }
                    break;
                case MAPPING:
                    MappingNode mappingNode = (MappingNode) realNode;
                    List<NodeTuple> map = mappingNode.getValue();
                    for (NodeTuple object : map) {
                        Node key = object.getKeyNode();
                        Node value = object.getValueNode();
                        anchorNode(key);
                        anchorNode(value);
                    }
                    break;
                default: // no further action required for non-collections
            }
        }
    }

    private void serializeNode(Node node) {
        if (node.getNodeType() == NodeType.ANCHOR) {
            node = ((AnchorNode) node).getRealNode();
        }
        Optional<Anchor> tAlias = Optional.ofNullable(this.anchors.get(node));
        if (this.serializedNodes.contains(node)) {
            this.emitable.emit(new AliasEvent(tAlias));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeType()) {
                case SCALAR:
                    ScalarNode scalarNode = (ScalarNode) node;
                    serializeComments(node.getBlockComments());
                    Tag detectedTag = settings.getScalarResolver().resolve(scalarNode.getValue(), true);
                    Tag defaultTag = settings.getScalarResolver().resolve(scalarNode.getValue(), false);
                    ImplicitTuple tuple = new ImplicitTuple(node.getTag().equals(detectedTag), node
                            .getTag().equals(defaultTag));
                    ScalarEvent event = new ScalarEvent(tAlias, Optional.of(node.getTag().getValue()), tuple,
                            scalarNode.getValue(), scalarNode.getScalarStyle());
                    this.emitable.emit(event);
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                case SEQUENCE:
                    SequenceNode seqNode = (SequenceNode) node;
                    serializeComments(node.getBlockComments());
                    boolean implicitS = node.getTag().equals(Tag.SEQ);
                    this.emitable.emit(new SequenceStartEvent(tAlias, Optional.of(node.getTag().getValue()),
                            implicitS, seqNode.getFlowStyle()));
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        serializeNode(item);
                    }
                    this.emitable.emit(new SequenceEndEvent());
                    serializeComments(node.getInLineComments());
                    serializeComments(node.getEndComments());
                    break;
                default:// instance of MappingNode
                    serializeComments(node.getBlockComments());
                    boolean implicitM = node.getTag().equals(Tag.MAP);
                    MappingNode mappingNode = (MappingNode) node;
                    List<NodeTuple> map = mappingNode.getValue();
                    if (mappingNode.getTag() != Tag.COMMENT) {
                        this.emitable.emit(new MappingStartEvent(tAlias, Optional.of(mappingNode.getTag().getValue()),
                                implicitM, mappingNode.getFlowStyle(), Optional.empty(), Optional.empty()));
                        for (NodeTuple entry : map) {
                            Node key = entry.getKeyNode();
                            Node value = entry.getValueNode();
                            serializeNode(key);
                            serializeNode(value);
                        }
                        this.emitable.emit(new MappingEndEvent());
                        serializeComments(node.getInLineComments());
                        serializeComments(node.getEndComments());
                    }
            }
        }
    }

    private void serializeComments(List<CommentLine> comments) {
        if (comments == null) {
            return;
        }
        for (CommentLine line : comments) {
            CommentEvent commentEvent = new CommentEvent(line.getCommentType(), line.getValue(), line.getStartMark(),
                    line.getEndMark());
            this.emitable.emit(commentEvent);
        }
    }
}
