package it.krzeminski.snakeyaml.engine.kmp.comments

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.constructor.StandardConstructor
import it.krzeminski.snakeyaml.engine.kmp.nodes.MappingNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader
import okio.Buffer

class ComposerWithCommentEnabledTest: FunSpec({
    test("empty") {
        val data = ""
        val expected = emptyList<String>()

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("parse with only comment") {
        val data = "# Comment"
        val expected = listOf(
            "Block Comment", //
            "MappingNode", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("comment ending a line") {
        val data = "" + //
        "key: # Comment\n" + //
        "  value\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "        ScalarNode: value", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("multiline comment") {
        val data = "" + //
            "key: # Comment\n" + //
            "     # lines\n" + //
            "  value\n" + //
            "\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        ScalarNode: value", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("blank line") {
        val data = "" + //
            "\n"
        val expected = listOf(
            "Block Comment", //
            "MappingNode", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("blank line comments") {
        val data = "" + //
            "\n" + //
            "abc: def # commment\n" + //
            "\n" + //
            "\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("block scalar") {
        val data = "" + //
            "abc: > # Comment\n" + //
            "    def\n" + //
            "    hij\n" + //
            "\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: abc", //
            "            InLine Comment", //
            "        ScalarNode: def hij", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("directive line end comment") {
        val data = "%YAML 1.1 #Comment\n---\n"
        val expected = listOf(
            "ScalarNode: ", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("sequence") {
        val data = "" + //
            "# Comment\n" + //
            "list: # InlineComment1\n" + //
            "# Block Comment\n" + //
            "- item # InlineComment2\n" + //
            "# Comment\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: list", //
            "            InLine Comment", //
            "        SequenceNode", //
            "            Block Comment", //
            "            ScalarNode: item", //
            "                InLine Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("all comments") {
        val data = "" + //
            "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "key: # Inline Comment1a\n" + //
            "     # Inline Comment1b\n" + //
            "  # Block Comment3a\n" + //
            "  # Block Comment3b\n" + //
            "  value # Inline Comment2\n" + //
            "# Block Comment4\n" + //
            "list: # InlineComment3a\n" + //
            "      # InlineComment3b\n" + //
            "# Block Comment5\n" + //
            "- item1 # InlineComment4\n" + //
            "- item2: [ value2a, value2b ] # InlineComment5\n" + //
            "- item3: { key3a: [ value3a1, value3a2 ], key3b: value3b } # InlineComment6\n" + //
            "# Block Comment6\n" + //
            "---\n" + //
            "# Block Comment7\n" + //
            ""
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: key", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: value", //
            "            InLine Comment", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: list", //
            "            InLine Comment", //
            "            InLine Comment", //
            "        SequenceNode", //
            "            Block Comment", //
            "            ScalarNode: item1", //
            "                InLine Comment", //
            "            MappingNode", //
            "                Tuple", //
            "                    ScalarNode: item2", //
            "                    SequenceNode", //
            "                        ScalarNode: value2a", //
            "                        ScalarNode: value2b", //
            "                        InLine Comment", //
            "            MappingNode", //
            "                Tuple", //
            "                    ScalarNode: item3", //
            "                    MappingNode", //
            "                        Tuple", //
            "                            ScalarNode: key3a", //
            "                            SequenceNode", //
            "                                ScalarNode: value3a1", //
            "                                ScalarNode: value3a2", //
            "                        Tuple", //
            "                            ScalarNode: key3b", //
            "                            ScalarNode: value3b", //
            "                        InLine Comment", //
            "End Comment", //
            "---", //
            "Block Comment", //
            "ScalarNode: ", // This is an empty scalar created as this is an empty document
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("all comments 2") {
        val data = "" + //
            "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "- item1 # Inline Comment1a\n" + //
            "        # Inline Comment1b\n" + //
            "# Block Comment3a\n" + //
            "# Block Comment3b\n" + //
            "- item2: value # Inline Comment2\n" + //
            "# Block Comment4\n" + //
            ""
        val expected = listOf(
            "SequenceNode", //
            "    Block Comment", //
            "    Block Comment", //
            "    ScalarNode: item1", //
            "        InLine Comment", //
            "        InLine Comment", //
            "    MappingNode", //
            "        Tuple", //
            "            Block Comment", //
            "            Block Comment", //
            "            ScalarNode: item2", //
            "            ScalarNode: value", //
            "                InLine Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("all comments 3") {
        val data = "" + //
        "# Block Comment1\n" + //
            "[ item1, item2: value2, {item3: value3} ] # Inline Comment1\n" + //
            "# Block Comment2\n" + //
            ""
        val expected = listOf(
            "Block Comment", //
            "SequenceNode", //
            "    ScalarNode: item1", //
            "    MappingNode", //
            "        Tuple", //
            "            ScalarNode: item2", //
            "            ScalarNode: value2", //
            "    MappingNode", //
            "        Tuple", //
            "            ScalarNode: item3", //
            "            ScalarNode: value3", //
            "    InLine Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("get single node") {
        val data = "" + //
            "\n" + //
            "abc: def # commment\n" + //
            "\n" + //
            "\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("get single node header comment") {
        val data = "" + //
            "\n" + //
            "# Block Comment1\n" + //
            "# Block Comment2\n" + //
            "abc: def # commment\n" + //
            "\n" + //
            "\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        Block Comment", //
            "        Block Comment", //
            "        Block Comment", //
            "        ScalarNode: abc", //
            "        ScalarNode: def", //
            "            InLine Comment", //
            "End Comment", //
            "End Comment", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = getNodeList(sut)

        assertNodesEqual(expected, result)
    }

    test("base constructor get data") {
        val data = "" + //
            "\n" + //
            "abc: def # comment\n" + //
            "\n" + //
            "\n"
        val sut = TestConstructor(LoadSettings.builder().build())
        val composer = newComposerWithCommentsEnabled(data)
        val result = sut.constructSingleDocument(composer.getSingleNode())
        val map = result.shouldBeInstanceOf<LinkedHashMap<String, Any>>()
        map.size shouldBe 1
        map["abc"] shouldBe "def"
    }

    test("empty entry in map") {
        val data = "userProps:\n" + //
        "#password\n" + //
            "pass: mySecret\n"
        val expected = listOf(
            "MappingNode", //
            "    Tuple", //
            "        ScalarNode: userProps", //
            "        ScalarNode: ", //
            "    Tuple", //
            "        Block Comment", //
            "        ScalarNode: pass", //
            "        ScalarNode: mySecret", //
        )

        val sut = newComposerWithCommentsEnabled(data)
        val result = listOf(sut.getSingleNode()!!)

        assertNodesEqual(expected, result)
    }
})

private fun newComposerWithCommentsEnabled(data: String): Composer {
    val settings = LoadSettings.builder().setParseComments(true).build()
    return Composer(settings, ParserImpl(settings, StreamReader(settings, data)))
}

private fun getNodeList(composer: Composer): List<Node> = buildList {
    while (composer.hasNext()) {
        add(composer.next())
    }
}

private fun assertNodesEqual(expected: List<String>, nodeList: List<Node>) {
    val buffer = Buffer()
    var first = true
    nodeList.forEach { node ->
        if (first) {
            first = false
        } else {
            buffer.writeUtf8("---\n")
        }
        buffer.printNodeInternal(node=node, level=0)
    }
    val actualString = buffer.readUtf8()
    val actuals = actualString.split("\n").filter { it.isNotBlank() }
    actuals shouldBe expected
}

private fun Buffer.printNodeInternal(node: Node, level: Int) {
    when (node) {
        is MappingNode -> {
            printBlockComment(node = node, level = level)
            printWithIndent(line = node::class.simpleName!!, level=level)
            node.value.forEach { childNodeTuple ->
                printWithIndent(line = "Tuple", level = level + 1)
                printNodeInternal(childNodeTuple.keyNode, level = level + 2)
                printNodeInternal(childNodeTuple.valueNode, level = level + 2)
            }
            printInLineComment(node = node, level = level)
            printEndComment(node = node, level = level)
        }
        is SequenceNode -> {
            printBlockComment(node = node, level = level)
            printWithIndent(line = node::class.simpleName!!, level = level)
            node.value.forEach { childNode ->
                printNodeInternal(childNode, level = level + 1)
            }
            printInLineComment(node = node, level = level)
            printEndComment(node = node, level = level)
        }
        is ScalarNode -> {
            printBlockComment(node = node, level = level)
            printWithIndent(line = "${node::class.simpleName!!}: ${node.value}", level = level)
            printInLineComment(node = node, level = level)
            printEndComment(node = node, level = level)
        }
        else -> {
            printBlockComment(node = node, level = level)
            printWithIndent(line = node::class.simpleName!!, level = level)
            printInLineComment(node = node, level = level)
            printEndComment(node = node, level = level)
        }
    }
}

private fun Buffer.printBlockComment(node: Node, level: Int) {
    repeat(node.blockComments?.size ?: 0) {
        this.printWithIndent(line = "Block Comment", level = level)
    }
}

private fun Buffer.printInLineComment(node: Node, level: Int) {
    repeat(node.inLineComments?.size ?: 0) {
        this.printWithIndent(line = "InLine Comment", level = level + 1)
    }
}

private fun Buffer.printEndComment(node: Node, level: Int) {
    repeat(node.endComments?.size ?: 0) {
        this.printWithIndent(line = "End Comment", level = level)
    }
}

private fun Buffer.printWithIndent(line: String, level: Int) {
    repeat(level) {
        this.writeUtf8("    ")
    }
    this.writeUtf8(line)
    this.writeUtf8("\n")
}

private class TestConstructor(settings: LoadSettings)
    : StandardConstructor(settings)
