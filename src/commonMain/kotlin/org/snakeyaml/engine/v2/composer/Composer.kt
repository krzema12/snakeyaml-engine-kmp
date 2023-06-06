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
package org.snakeyaml.engine.v2.composer

import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.comments.CommentEventsCollector
import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.Anchor
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.events.AliasEvent
import org.snakeyaml.engine.v2.events.Event
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.NodeEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.exceptions.ComposerException
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeTuple
import org.snakeyaml.engine.v2.nodes.NodeType
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.parser.Parser
import org.snakeyaml.engine.v2.resolver.ScalarResolver

/**
 * Creates a node graph from parser events.
 *
 * Corresponds to the 'Composer' step as described in chapter 3.1.2 of the
 * [YAML Specification](http://www.yaml.org/spec/1.2/spec.html#id2762107).
 *
 * It implements [Iterator] to get the stream of [Node]s from the input.
 */
class Composer(
    private val settings: LoadSettings,
    /** Event parser */
    private val parser: Parser,
) : Iterator<Node> {
    private val scalarResolver: ScalarResolver = settings.schema.scalarResolver
    private val anchors: MutableMap<Anchor, Node> = mutableMapOf()
    private val recursiveNodes: MutableSet<Node> = mutableSetOf()
    private val blockCommentsCollector: CommentEventsCollector =
        CommentEventsCollector(parser, CommentType.BLANK_LINE, CommentType.BLOCK)
    private val inlineCommentsCollector: CommentEventsCollector =
        CommentEventsCollector(parser, CommentType.IN_LINE)
    private var nonScalarAliasesCount = 0

    /**
     * Checks if further documents are available.
     *
     * @return `true` if there is at least one more document.
     */
    override fun hasNext(): Boolean {
        // Drop the STREAM-START event.
        if (parser.checkEvent(Event.ID.StreamStart)) {
            parser.next()
        }
        // If there are more documents available?
        return !parser.checkEvent(Event.ID.StreamEnd)
    }

    /**
     * Reads a document from a source that contains only one document.
     *
     * If the stream contains more than one document an exception is thrown.
     *
     * @return The root node of the document or `null` if no document is
     * available.
     */
    val singleNode: Node?
        get() {
            // Drop the STREAM-START event.
            parser.next()
            // Compose a document if the stream is not empty.
            val document = if (!parser.checkEvent(Event.ID.StreamEnd)) next() else null
            // Ensure that the stream contains no more documents.
            if (!parser.checkEvent(Event.ID.StreamEnd)) {
                val event = parser.next()
                val previousDocMark = document?.startMark
                throw ComposerException(
                    problem = "expected a single document in the stream",
                    problemMark = previousDocMark,
                    context = "but found another document",
                    contextMark = event.startMark,
                )
            }
            // Drop the STREAM-END event.
            parser.next()
            return document
        }

    /**
     * Reads and composes the next document.
     *
     * @return The root node of the document or `null` if no more documents are available.
     */
    override fun next(): Node {
        // Collect inter-document start comments
        blockCommentsCollector.collectEvents()
        if (parser.checkEvent(Event.ID.StreamEnd)) {
            val commentLines = blockCommentsCollector.consume()
            val startMark = commentLines.first().startMark
            val children = mutableListOf<NodeTuple>()
            val node: Node = MappingNode(
                tag = Tag.COMMENT,
                resolved = false,
                value = children,
                flowStyle = FlowStyle.BLOCK,
                startMark = startMark,
                endMark = null,
            )
            node.blockComments = (commentLines)
            return node
        }
        // Drop the DOCUMENT-START event.
        parser.next()
        // Compose the root node.
        val node = composeNode(null)
        // Drop the DOCUMENT-END event.
        blockCommentsCollector.collectEvents()
        if (!blockCommentsCollector.isEmpty()) {
            node.endComments = blockCommentsCollector.consume()
        }
        parser.next()
        anchors.clear()
        recursiveNodes.clear()
        nonScalarAliasesCount = 0
        return node
    }

    private fun composeNode(parent: Node?): Node {
        blockCommentsCollector.collectEvents()
        parent?.let { e: Node ->
            recursiveNodes.add(e) // TODO add unit test for this line
        }
        val node: Node
        if (parser.checkEvent(Event.ID.Alias)) {
            val event = parser.next() as AliasEvent
            val anchor: Anchor = event.alias
            node = anchors[anchor] ?: throw ComposerException("found undefined alias $anchor", event.startMark)
            if (node.nodeType != NodeType.SCALAR) {
                nonScalarAliasesCount++
                if (nonScalarAliasesCount > settings.maxAliasesForCollections) {
                    throw YamlEngineException(
                        "Number of aliases for non-scalar nodes exceeds the specified max=${settings.maxAliasesForCollections}",
                    )
                }
            }
            if (recursiveNodes.remove(node)) {
                node.isRecursive = true
            }
            // drop comments, they can not be supported here
            blockCommentsCollector.consume()
            inlineCommentsCollector.collectEvents().consume()
        } else {
            val event = parser.peekEvent() as NodeEvent
            val anchor: Anchor? = event.anchor
            // the check for duplicate anchors has been removed (issue 174)
            node = if (parser.checkEvent(Event.ID.Scalar)) {
                composeScalarNode(anchor, blockCommentsCollector.consume())
            } else if (parser.checkEvent(Event.ID.SequenceStart)) {
                composeSequenceNode(anchor)
            } else {
                composeMappingNode(anchor)
            }
        }
        parent?.let { o: Node ->
            recursiveNodes.remove(o) // TODO add unit test for this line
        }
        return node
    }

    private fun registerAnchor(anchor: Anchor, node: Node) {
        anchors[anchor] = node
        node.anchor = anchor
    }

    /**
     * Create [ScalarNode]
     *
     * @param anchor - anchor if present
     * @param blockComments - comments before the Node
     * @return Node
     */
    private fun composeScalarNode(anchor: Anchor?, blockComments: List<CommentLine>): ScalarNode {
        val ev = parser.next() as ScalarEvent
        val tag: String? = ev.tag
        val resolved: Boolean
        val nodeTag: Tag
        if (tag == null || tag == "!") {
            nodeTag = scalarResolver.resolve(ev.value, ev.implicit.canOmitTagInPlainScalar())
            resolved = true
        } else {
            nodeTag = Tag(tag)
            resolved = false
        }
        val node = ScalarNode(
            tag = nodeTag,
            resolved = resolved,
            value = ev.value,
            scalarStyle = ev.scalarStyle,
            startMark = ev.startMark,
            endMark = ev.endMark,
        )
        anchor?.let { a: Anchor ->
            registerAnchor(a, node)
        }
        node.blockComments = (blockComments)
        node.inLineComments = (inlineCommentsCollector.collectEvents().consume())
        return node
    }

    /**
     * Compose a [SequenceNode] from the input starting with [SequenceStartEvent]
     *
     * @param anchor - anchor if present
     * @return parsed Node
     */
    private fun composeSequenceNode(anchor: Anchor?): SequenceNode {
        val startEvent = parser.next() as SequenceStartEvent
        val tag: String? = startEvent.tag
        val nodeTag: Tag
        val resolved: Boolean
        if (tag == null || tag == "!") {
            nodeTag = Tag.SEQ
            resolved = true
        } else {
            nodeTag = Tag(tag)
            resolved = false
        }
        val children = ArrayList<Node>()
        val node = SequenceNode(
            tag = nodeTag,
            value = children,
            flowStyle = startEvent.flowStyle,
            resolved = resolved,
            startMark = startEvent.startMark,
            endMark = null,
        )
        if (startEvent.isFlow()) {
            node.blockComments = (blockCommentsCollector.consume())
        }
        anchor?.let { a: Anchor ->
            registerAnchor(a, node)
        }
        while (!parser.checkEvent(Event.ID.SequenceEnd)) {
            blockCommentsCollector.collectEvents()
            if (parser.checkEvent(Event.ID.SequenceEnd)) {
                break
            }
            children.add(composeNode(node))
        }
        if (startEvent.isFlow()) {
            node.inLineComments = (inlineCommentsCollector.collectEvents().consume())
        }
        val endEvent = parser.next()
        node.setEndMark(endEvent.endMark)
        inlineCommentsCollector.collectEvents()
        if (!inlineCommentsCollector.isEmpty()) {
            node.inLineComments = (inlineCommentsCollector.consume())
        }
        return node
    }

    /**
     * Create [MappingNode]
     *
     * @param anchor - anchor if present
     */
    private fun composeMappingNode(anchor: Anchor?): MappingNode {
        val startEvent = parser.next() as MappingStartEvent
        val tag: String? = startEvent.tag
        val nodeTag: Tag
        val resolved: Boolean
        if (tag == null || tag == "!") {
            nodeTag = Tag.MAP
            resolved = true
        } else {
            nodeTag = Tag(tag)
            resolved = false
        }
        val children: MutableList<NodeTuple> = ArrayList()
        val node = MappingNode(
            tag = nodeTag,
            resolved = resolved,
            value = children,
            flowStyle = startEvent.flowStyle,
            startMark = startEvent.startMark,
            endMark = null,
        )
        if (startEvent.isFlow()) {
            node.blockComments = (blockCommentsCollector.consume())
        }
        anchor?.let { a: Anchor ->
            registerAnchor(a, node)
        }
        while (!parser.checkEvent(Event.ID.MappingEnd)) {
            blockCommentsCollector.collectEvents()
            if (parser.checkEvent(Event.ID.MappingEnd)) {
                break
            }
            composeMappingChildren(children, node)
        }
        if (startEvent.isFlow()) {
            node.inLineComments = (inlineCommentsCollector.collectEvents().consume())
        }
        val endEvent = parser.next()
        node.setEndMark(endEvent.endMark)
        inlineCommentsCollector.collectEvents()
        if (!inlineCommentsCollector.isEmpty()) {
            node.inLineComments = (inlineCommentsCollector.consume())
        }
        return node
    }

    /**
     * Add the provided [node] to the children as the last child
     *
     * @param children - the list to be extended
     * @param node - the child to the children
     */
    private fun composeMappingChildren(children: MutableList<NodeTuple>, node: MappingNode) {
        val itemKey = composeKeyNode(node)
        val itemValue = composeValueNode(node)
        children.add(NodeTuple(itemKey, itemValue))
    }

    /**
     * To be able to override `composeNode(node)` which is a key
     *
     * @param node - the source
     * @return node
     */
    private fun composeKeyNode(node: MappingNode): Node = composeNode(node)

    /**
     * To be able to override `composeNode(node)` which is a value
     *
     * @param node - the source
     * @return node
     */
    private fun composeValueNode(node: MappingNode): Node = composeNode(node)
}
