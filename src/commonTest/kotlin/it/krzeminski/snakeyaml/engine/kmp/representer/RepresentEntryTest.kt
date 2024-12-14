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
package it.krzeminski.snakeyaml.engine.kmp.representer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentLine
import it.krzeminski.snakeyaml.engine.kmp.comments.CommentType
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.emitter.Emitter
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeTuple
import it.krzeminski.snakeyaml.engine.kmp.serializer.Serializer

class RepresentEntryTest : FunSpec({
    val settings = DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN)
        .setDefaultFlowStyle(FlowStyle.BLOCK).setDumpComments(true).build()

    val commentedEntryRepresenter = CommentedEntryRepresenter(settings)

    fun createMap(): Map<String, String> = mapOf(
        "a" to "val1",
    )

    test("Represent and dump mapping nodes using the new method").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val stringOutputStream = StringOutputStream()

        val serializer = Serializer(settings, Emitter(settings, stringOutputStream))
        serializer.emitStreamStart()
        serializer.serializeDocument(commentedEntryRepresenter.represent(createMap()))
        serializer.emitStreamEnd()

        stringOutputStream.toString() shouldBe "#Key node block comment\n" + "a: val1 #Value node inline comment\n"
    }
})

private class CommentedEntryRepresenter(settings: DumpSettings) : CommonRepresenter(settings) {
    override fun Map.Entry<*, *>.toNodeTuple(): NodeTuple {
        val tuple: NodeTuple = super.toNodeTuple(this)
        val keyBlockComments: List<CommentLine> = listOf(
            CommentLine(
                null, null,
                "Key node block comment", CommentType.BLOCK
            )
        )
        tuple.keyNode.blockComments = keyBlockComments

        val valueEndComments: List<CommentLine> = listOf(
            CommentLine(
                null, null,
                "Value node inline comment", CommentType.IN_LINE
            )
        )
        tuple.valueNode.endComments = valueEndComments

        return tuple
    }
}

private class StringOutputStream(
    private val builder: StringBuilder = StringBuilder(),
) : StreamDataWriter {
    override fun write(str: String) {
        builder.append(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        builder.append(str, off, len)
    }

    override fun toString(): String = builder.toString()
}
