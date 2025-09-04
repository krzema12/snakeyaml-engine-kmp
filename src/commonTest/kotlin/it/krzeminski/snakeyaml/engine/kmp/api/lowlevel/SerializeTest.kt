package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.compareEvents
import it.krzeminski.snakeyaml.engine.kmp.events.*
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class SerializeTest : FunSpec({
    test("serialize one scalar") {
        val serialize = Serialize(DumpSettings.builder().build())
        val events = serialize.serializeOne(ScalarNode(Tag.STR, "a", ScalarStyle.PLAIN))
        val list = events.toList()
        list.size shouldBe 5
        compareEvents(
            listOf(
                StreamStartEvent(),
                DocumentStartEvent(false, null, emptyMap()),
                ScalarEvent(null, null, ImplicitTuple(false, false), "a", ScalarStyle.PLAIN),
                DocumentEndEvent(false),
                StreamEndEvent()
            ),
            list
        )
    }
})
