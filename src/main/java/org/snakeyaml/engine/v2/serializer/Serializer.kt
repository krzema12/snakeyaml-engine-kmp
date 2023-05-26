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
import org.snakeyaml.engine.v2.nodes.NodeTuple
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
        settings.explicitRootTag.ifPresent { tag: Tag ->
            node.tag = tag
        }
        serializeNode(node)
        emitable.emit(DocumentEndEvent(settings.isExplicitEnd))
        serializedNodes.clear()
        anchors.clear()
    }

    /**
     * Emit [StreamStartEvent]
     */
    fun emitStreamStart() {
        emitable.emit(StreamStartEvent())
    }

    /**
     * Emit [StreamEndEvent]
     */
    fun emitStreamEnd() {
        emitable.emit(StreamEndEvent())
    }

    private fun anchorNode(node: Node) {
        val realNode = (node as? AnchorNode)?.realNode ?: node

        if (anchors.containsKey(realNode)) {
            // it looks weird, anchor does contain the key node, but we call computeIfAbsent()
            // this is because the value is null (HashMap permits values to be null)
            anchors.computeIfAbsent(realNode) { settings.anchorGenerator.nextAnchor(realNode) }
        } else {
            anchors[realNode] =
                if (realNode.anchor.isPresent) settings.anchorGenerator.nextAnchor(realNode) else null
            when (realNode.nodeType) {
                NodeType.SEQUENCE -> {
                    val seqNode = realNode as SequenceNode
                    val list: List<Node> = seqNode.value
                    for (item in list) {
                        anchorNode(item)
                    }
                }

                NodeType.MAPPING  -> {
                    val mappingNode = realNode as MappingNode
                    val map: List<NodeTuple> = mappingNode.value
                    for (`object` in map) {
                        val key: Node = `object`.keyNode
                        val value: Node = `object`.valueNode
                        anchorNode(key)
                        anchorNode(value)
                    }
                }

                else              -> {}
            }
        }
    }

    /**
     * Recursive serialization of a [Node]
     *
     * @param node - content
     */
    private fun serializeNode(node: Node) {
        val realNode = (node as? AnchorNode)?.realNode ?: node

        val tAlias = Optional.ofNullable(anchors[realNode])
        if (serializedNodes.contains(realNode)) {
            emitable.emit(AliasEvent(tAlias))
        } else {
            serializedNodes.add(realNode)
            when (realNode.nodeType) {
                NodeType.SCALAR   -> {
                    val scalarNode = realNode as ScalarNode
                    serializeComments(realNode.blockComments)
                    val detectedTag: Tag = settings.schema.scalarResolver.resolve(scalarNode.value, true)
                    val defaultTag: Tag = settings.schema.scalarResolver.resolve(scalarNode.value, false)
                    val tuple = ImplicitTuple(
                        realNode.tag == detectedTag,
                        realNode.tag == defaultTag,
                    )
                    val event = ScalarEvent(
                        tAlias, Optional.of<String>(realNode.tag.value), tuple,
                        scalarNode.value, scalarNode.scalarStyle,
                    )
                    emitable.emit(event)
                    serializeComments(realNode.inLineComments)
                    serializeComments(realNode.endComments)
                }

                NodeType.SEQUENCE -> {
                    val seqNode = realNode as SequenceNode
                    serializeComments(realNode.blockComments)
                    val implicitS = realNode.tag == Tag.SEQ
                    emitable.emit(
                        SequenceStartEvent(
                            tAlias, Optional.of<String>(realNode.tag.value),
                            implicitS, seqNode.flowStyle,
                        ),
                    )
                    val list: List<Node> = seqNode.value
                    for (item in list) {
                        serializeNode(item)
                    }
                    emitable.emit(SequenceEndEvent())
                    serializeComments(realNode.inLineComments)
                    serializeComments(realNode.endComments)
                }

                else              -> {
                    serializeComments(realNode.blockComments)
                    val implicitM = realNode.tag == Tag.MAP
                    val mappingNode = realNode as MappingNode
                    val map: List<NodeTuple> = mappingNode.value
                    if (mappingNode.tag !== Tag.COMMENT) {
                        emitable
                            .emit(
                                MappingStartEvent(
                                    tAlias,
                                    Optional.of<String>(mappingNode.tag.value),
                                    implicitM,
                                    mappingNode.flowStyle,
                                    Optional.empty<Mark>(),
                                    Optional.empty<Mark>(),
                                ),
                            )
                        for (entry in map) {
                            val key: Node = entry.keyNode
                            val value: Node = entry.valueNode
                            serializeNode(key)
                            serializeNode(value)
                        }
                        emitable.emit(MappingEndEvent())
                        serializeComments(realNode.inLineComments)
                        serializeComments(realNode.endComments)
                    }
                }
            }
        }
    }

    private fun serializeComments(comments: List<CommentLine>?) {
        if (comments == null) {
            return
        }
        for (line in comments) {
            val commentEvent = CommentEvent(
                line.commentType,
                line.value,
                line.startMark,
                line.endMark,
            )
            emitable.emit(commentEvent)
        }
    }
}
