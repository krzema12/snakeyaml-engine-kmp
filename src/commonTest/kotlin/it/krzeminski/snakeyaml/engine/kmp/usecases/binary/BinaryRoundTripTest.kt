package it.krzeminski.snakeyaml.engine.kmp.usecases.binary

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Serialize
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.events.Event
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.nodes.NodeType
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter

class BinaryRoundTripTest : FunSpec({
    test("binary") {
        val dumper = Dump(DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.BINARY).build())
        val source = "\u0096"
        val serialized = dumper.dumpToString(source)
        serialized shouldBe "!!binary |-\n  wpY=\n"

        // parse back to bytes
        val loader = Load()
        val deserialized = loader.loadOne(serialized)
        deserialized.shouldBeInstanceOf<ByteArray>()
        String(deserialized, Charsets.UTF_8) shouldBe source
    }

    test("binary node") {
        val source = "\u0096"
        val standardRepresenter = StandardRepresenter(
            DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.BINARY).build()
        )
        val scalar = standardRepresenter.represent(source) as ScalarNode

        // check Node
        scalar.tag shouldBe Tag.BINARY
        scalar.nodeType shouldBe NodeType.SCALAR
        scalar.value shouldBe "wpY="

        // check Event
        val serialize = Serialize(DumpSettings.builder().build())
        val eventsIter = serialize.serializeOne(scalar)
        val events = eventsIter.toList()
        events.size shouldBe 5
        val data = events[2] as ScalarEvent
        data.tag shouldBe Tag.BINARY.toString()
        data.scalarStyle shouldBe ScalarStyle.LITERAL
        data.value shouldBe "wpY="
        val implicit = data.implicit
        implicit.canOmitTagInPlainScalar() shouldBe false
        implicit.canOmitTagInNonPlainScalar() shouldBe false
    }

    test("str node") {
        val standardRepresenter = StandardRepresenter(DumpSettings.builder().build())
        val source = "\u0096"
        val scalar = standardRepresenter.represent(source) as ScalarNode
        val node = standardRepresenter.represent(source)
        node.tag shouldBe Tag.STR
        node.nodeType shouldBe NodeType.SCALAR
        scalar.value shouldBe "\u0096"
    }

    test("round trip binary") {
        val dumper = Dump(DumpSettings.builder().setNonPrintableStyle(NonPrintableStyle.ESCAPE).build())
        val toSerialized = mapOf("key" to "a\u0096b")
        val output = dumper.dumpToString(toSerialized)
        output shouldBe "{key: \"a\\x96b\"}\n"

        val loader = Load()
        val parsed = loader.loadOne(output) as Map<String, String>
        parsed["key"] shouldBe toSerialized["key"]
        parsed shouldBe toSerialized
    }
})
