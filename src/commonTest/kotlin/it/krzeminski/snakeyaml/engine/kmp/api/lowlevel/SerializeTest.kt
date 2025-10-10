package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.compareEvents
import it.krzeminski.snakeyaml.engine.kmp.events.*
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class SerializeTest : FunSpec({
    test("serialize one scalar") {
        val serialize = Serialize(DumpSettings())
        val events = serialize.serializeOne(ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN))
        val list = events.toList()
        list shouldHaveSize 5
        compareEvents(
            listOf(
                StreamStartEvent(),
                DocumentStartEvent(explicit = false, specVersion = null, tags = emptyMap()),
                ScalarEvent(
                    anchor = null,
                    tag = null,
                    implicit = ImplicitTuple(false, false),
                    value = "a",
                    scalarStyle = ScalarStyle.PLAIN
                ),
                DocumentEndEvent(isExplicit = false),
                StreamEndEvent()
            ),
            list
        )
    }
})
