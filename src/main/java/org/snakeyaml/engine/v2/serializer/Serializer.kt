/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.snakeyaml.engine.v2.serializer

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.emitter.Emitable
import org.snakeyaml.engine.v2.events.AliasEvent
import org.snakeyaml.engine.v2.events.CommentEvent
import org.snakeyaml.engine.v2.events.DocumentEndEvent
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.ImplicitTuple
import org.snakeyaml.engine.v2.events.MappingEndEvent
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceEndEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.events.StreamEndEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import org.snakeyaml.engine.v2.exceptions.Mark
import org.snakeyaml.engine.v2.nodes.AnchorNode
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeType
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.util.Optional

/**
 * Transform a [Node] Graph to [org.snakeyaml.engine.v2.events.Event] stream and allow provided [Emitable] to present the
 * [org.snakeyaml.engine.v2.events.Event]s into the output stream
 *
 * @param settings - dump configuration
 * @param emitable - destination for the event stream
 */
class Serializer(
    private val settings: DumpSettings,
    private val emitable: Emitable,
) {
    private val serializedNodes: MutableSet<Node> = mutableSetOf()
    private val anchors: MutableMap<Node, Anchor?> = mutableMapOf()

    /**
     * Serialize document
     *
     * @param node - the document root
     */
    fun serializeDocument(node: Node) {
        emitable.emit(
            DocumentStartEvent(
                settings.isExplicitStart,
                settings.yamlDirective,
                settings.tagDirective,
            ),
        )
        anchorNode(node)
        settings.explicitRootTag.ifPresent { tag -> node.tag = tag }
        serializeNode(node)
        emitable.emit(DocumentEndEvent(settings.isExplicitEnd))
        serializedNodes.clear()
        anchors.clear()
    }

    /** Emit [StreamStartEvent] */
    fun emitStreamStart() {
        emitable.emit(StreamStartEvent())
    }

    /** Emit [StreamEndEvent] */
    fun emitStreamEnd() {
        emitable.emit(StreamEndEvent())
    }

    private fun anchorNode(node: Node) {
        val realNode = if (node is AnchorNode) node.realNode else node

        if (anchors.containsKey(realNode)) {
            // it looks weird, anchor does contain the key node, but we call computeIfAbsent()
            // this is because the value is null (HashMap permits values to be null)
            anchors.computeIfAbsent(realNode) { settings.anchorGenerator.nextAnchor(realNode) }
        } else {
            anchors[realNode] =
                if (realNode.anchor.isPresent) {
                    settings.anchorGenerator.nextAnchor(realNode)
                } else {
                    null
                }
            when (realNode.nodeType) {
                NodeType.SEQUENCE                -> {
                    require(realNode is SequenceNode)
                    for (item in realNode.value) {
                        anchorNode(item)
                    }
                }

                NodeType.MAPPING                 -> {
                    require(realNode is MappingNode)
                    for ((key, value) in realNode.value) {
                        anchorNode(key)
                        anchorNode(value)
                    }
                }

                NodeType.SCALAR, NodeType.ANCHOR -> {}
            }
        }
    }

    /**
     * Recursive serialization of a [Node]
     *
     * @param node - content
     */
    private fun serializeNode(node: Node) {
        val realNode = if (node is AnchorNode) node.realNode else node

        val tAlias = Optional.ofNullable(anchors[realNode])
        if (serializedNodes.contains(realNode)) {
            emitable.emit(AliasEvent(tAlias))
        } else {
            serializedNodes.add(realNode)
            when (realNode.nodeType) {
                NodeType.SCALAR   -> {
                    require(realNode is ScalarNode)
                    serializeComments(realNode.blockComments)
                    val detectedTag = settings.schema.scalarResolver.resolve(realNode.value, true)
                    val defaultTag = settings.schema.scalarResolver.resolve(realNode.value, false)
                    val tuple = ImplicitTuple(
                        plain = realNode.tag == detectedTag,
                        nonPlain = realNode.tag == defaultTag,
                    )
                    val event = ScalarEvent(
                        anchor = tAlias,
                        tag = Optional.of(realNode.tag.value),
                        implicit = tuple,
                        value = realNode.value,
                        scalarStyle = realNode.scalarStyle,
                    )
                    emitable.emit(event)
                    serializeComments(realNode.inLineComments)
                    serializeComments(realNode.endComments)
                }

                NodeType.SEQUENCE -> {
                    require(realNode is SequenceNode)
                    serializeComments(realNode.blockComments)
                    val implicitS = realNode.tag == Tag.SEQ
                    emitable.emit(
                        SequenceStartEvent(
                            anchor = tAlias,
                            tag = Optional.of(realNode.tag.value),
                            implicit = implicitS,
                            flowStyle = realNode.flowStyle,
                        ),
                    )
                    for (item in realNode.value) {
                        serializeNode(item)
                    }
                    emitable.emit(SequenceEndEvent())
                    serializeComments(realNode.inLineComments)
                    serializeComments(realNode.endComments)
                }

                NodeType.MAPPING  -> {
                    require(realNode is MappingNode)
                    serializeComments(realNode.blockComments)
                    if (realNode.tag != Tag.COMMENT) {
                        emitable.emit(
                            MappingStartEvent(
                                anchor = tAlias,
                                tag = Optional.of(realNode.tag.value),
                                implicit = realNode.tag == Tag.MAP,
                                flowStyle = realNode.flowStyle,
                                startMark = Optional.empty<Mark>(),
                                endMark = Optional.empty<Mark>(),
                            ),
                        )
                        for ((key, value) in realNode.value) {
                            serializeNode(key)
                            serializeNode(value)
                        }
                        emitable.emit(MappingEndEvent())
                        serializeComments(realNode.inLineComments)
                        serializeComments(realNode.endComments)
                    }
                }

                else              -> {
                    error("Could not handle node type ${realNode.nodeType}")
                }
            }
        }
    }

    private fun serializeComments(comments: List<CommentLine>?) {
        if (comments == null) return

        for (line in comments) {
            val commentEvent = CommentEvent(
                commentType = line.commentType,
                value = line.value,
                startMark = line.startMark,
                endMark = line.endMark,
            )
            emitable.emit(commentEvent)
        }
    }
}
