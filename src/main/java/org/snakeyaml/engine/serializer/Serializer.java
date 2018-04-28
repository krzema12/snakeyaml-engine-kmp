/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.snakeyaml.engine.api.DumpSettings;
import org.snakeyaml.engine.common.Anchor;
import org.snakeyaml.engine.events.AliasEvent;
import org.snakeyaml.engine.events.DocumentEndEvent;
import org.snakeyaml.engine.events.DocumentStartEvent;
import org.snakeyaml.engine.events.Event;
import org.snakeyaml.engine.events.ImplicitTuple;
import org.snakeyaml.engine.events.MappingEndEvent;
import org.snakeyaml.engine.events.MappingStartEvent;
import org.snakeyaml.engine.events.ScalarEvent;
import org.snakeyaml.engine.events.SequenceEndEvent;
import org.snakeyaml.engine.events.SequenceStartEvent;
import org.snakeyaml.engine.nodes.AnchorNode;
import org.snakeyaml.engine.nodes.CollectionNode;
import org.snakeyaml.engine.nodes.MappingNode;
import org.snakeyaml.engine.nodes.Node;
import org.snakeyaml.engine.nodes.NodeTuple;
import org.snakeyaml.engine.nodes.NodeType;
import org.snakeyaml.engine.nodes.ScalarNode;
import org.snakeyaml.engine.nodes.SequenceNode;
import org.snakeyaml.engine.nodes.Tag;

public class Serializer {
    private final DumpSettings settings;
    private final List<Event> emitter = new ArrayList();
    private Set<Node> serializedNodes;
    private Map<Node, Anchor> anchors;

    public Serializer(DumpSettings settings) {
        this.settings = settings;
        this.serializedNodes = new HashSet<Node>();
        this.anchors = new HashMap<Node, Anchor>();
    }

    public List<Event> serialize(Node node) {
        this.emitter.add(new DocumentStartEvent( settings.isExplicitStart(), settings.getSpecVersion(), settings.getUseTags()));
        anchorNode(node);
        settings.getExplicitRootTag().ifPresent(tag -> node.setTag(tag));
        serializeNode(node, Optional.empty());
        this.emitter.add(new DocumentEndEvent(settings.isExplicitEnd()));
        this.serializedNodes.clear();
        this.anchors.clear();
        return emitter;
    }

    private void anchorNode(Node node) {
        if (node.getNodeType() == NodeType.ANCHOR) {
            node = ((AnchorNode) node).getRealNode();
        }
        if (this.anchors.containsKey(node)) {
            Anchor anchor = this.anchors.get(node);
            if (null == anchor) {
                anchor = settings.getAnchorGenerator().nextAnchor(node);
                this.anchors.put(node, anchor);
            }
        } else {
            this.anchors.put(node, null);
            switch (node.getNodeType()) {
                case SEQUENCE:
                    SequenceNode seqNode = (SequenceNode) node;
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        anchorNode(item);
                    }
                    break;
                case MAPPING:
                    MappingNode mnode = (MappingNode) node;
                    List<NodeTuple> map = mnode.getValue();
                    for (NodeTuple object : map) {
                        Node key = object.getKeyNode();
                        Node value = object.getValueNode();
                        anchorNode(key);
                        anchorNode(value);
                    }
                    break;
            }
        }
    }

    // parent is not used
    private void serializeNode(Node node, Optional<Node> parent) {
        if (node.getNodeType() == NodeType.ANCHOR) {
            node = ((AnchorNode) node).getRealNode();
        }
        Optional<Anchor> tAlias = Optional.ofNullable(this.anchors.get(node));
        if (this.serializedNodes.contains(node)) {
            this.emitter.add(new AliasEvent(tAlias));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeType()) {
                case SCALAR:
                    ScalarNode scalarNode = (ScalarNode) node;
                    Tag detectedTag = settings.getScalarResolver().resolve(scalarNode.getValue(), true);
                    Tag defaultTag = settings.getScalarResolver().resolve(scalarNode.getValue(), false);
                    ImplicitTuple tuple = new ImplicitTuple(node.getTag().equals(detectedTag), node
                            .getTag().equals(defaultTag));
                    ScalarEvent event = new ScalarEvent(tAlias, Optional.of(node.getTag().getValue()), tuple,
                            scalarNode.getValue(),  scalarNode.getStyle());
                    this.emitter.add(event);
                    break;
                case SEQUENCE:
                    SequenceNode seqNode = (SequenceNode) node;
                    boolean implicitS = node.getTag().equals(Tag.SEQ);
                    this.emitter.add(new SequenceStartEvent(tAlias, Optional.of(node.getTag().getValue()),
                            implicitS,  seqNode.getFlowStyle()));
                    List<Node> list = seqNode.getValue();
                    for (Node item : list) {
                        serializeNode(item, Optional.of(node));
                    }
                    this.emitter.add(new SequenceEndEvent());
                    break;
                default:// instance of MappingNode
                    boolean implicitM = node.getTag().equals(Tag.MAP);
                    this.emitter.add(new MappingStartEvent(tAlias, Optional.of(node.getTag().getValue()),
                            implicitM,  ((CollectionNode) node).getFlowStyle()));
                    MappingNode mappingNode = (MappingNode) node;
                    List<NodeTuple> map = mappingNode.getValue();
                    for (NodeTuple entry : map) {
                        Node key = entry.getKeyNode();
                        Node value = entry.getValueNode();
                        serializeNode(key, Optional.of(mappingNode));
                        serializeNode(value, Optional.of(mappingNode));
                    }
                    this.emitter.add(new MappingEndEvent());
            }
        }
    }
}
