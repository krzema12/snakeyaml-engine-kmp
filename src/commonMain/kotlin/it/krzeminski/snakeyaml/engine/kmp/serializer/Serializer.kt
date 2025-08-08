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
package it.krzeminski.snakeyaml.engine.kmp.serializer

import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentLine
import it.krzeminski.snakeyaml.engine.kmp.common.Anchor
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitable
import it.krzeminski.snakeyaml.engine.kmp.events.*
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.MergeUtils
import it.krzeminski.snakeyaml.engine.kmp.nodes.*

/**
 * Transform a [Node] Graph to [it.krzeminski.snakeyaml.engine.kmp.events.Event] stream and allow provided [Emitable] to present the
 * [it.krzeminski.snakeyaml.engine.kmp.events.Event]s into the output stream
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
    private val isDereferenceAliases: Boolean = settings.isDereferenceAliases
    private val recursive: IdentitySet<Node> = IdentitySet()
    private val mergeUtils = MergeUtils(asMappingNode = { node ->
        if (node is MappingNode) {
            return@MergeUtils node
        }
        // TODO: This need to be explored more to understand if only MappingNode possible.
        //       Or at least the error message needs to be improved.
        throw YamlEngineException("expecting MappingNode while processing merge.")
    })

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
        if (settings.explicitRootTag != null) {
            node.tag = settings.explicitRootTag
        }
        serializeNode(node)
        emitable.emit(DocumentEndEvent(settings.isExplicitEnd))
        serializedNodes.clear()
        anchors.clear()
        recursive.clear()
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
            anchors.getOrPut(realNode) { settings.anchorGenerator.nextAnchor(realNode) }
        } else {
            anchors[realNode] =
                if (realNode.anchor != null) {
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


        if (isDereferenceAliases && recursive.contains(node)) {
            throw YamlEngineException("Cannot dereference aliases for recursive structures.")
        }
        recursive.add(node)
        val tAlias = if (!isDereferenceAliases) {
            anchors[realNode]
        } else {
            null
        }

        if (!isDereferenceAliases && serializedNodes.contains(realNode)) {
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
                        tag = realNode.tag.value,
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
                            tag = realNode.tag.value,
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
                        var map = realNode.value
                        if (this.isDereferenceAliases && realNode.hasMergeTag) {
                            map = mergeUtils.flatten(realNode)
                        }

                        emitable.emit(
                            MappingStartEvent(
                                anchor = tAlias,
                                tag = realNode.tag.value,
                                implicit = realNode.tag == Tag.MAP,
                                flowStyle = realNode.flowStyle,
                                startMark = null,
                                endMark = null,
                            ),
                        )
                        for ((key, value) in map) {
                            serializeNode(key)
                            serializeNode(value)
                        }
                        emitable.emit(MappingEndEvent())
                        serializeComments(realNode.inLineComments)
                        serializeComments(realNode.endComments)
                    }
                }

                NodeType.ANCHOR   -> {}
            }
        }
        recursive.remove(node)
    }

    private fun serializeComments(comments: List<CommentLine>?) {
        if (settings.dumpComments && comments != null) {
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
}
