package it.krzeminski.snakeyaml.engine.kmp.emitter

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.StringStreamDataWriter
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.events.DocumentStartEvent
import it.krzeminski.snakeyaml.engine.kmp.events.ImplicitTuple
import it.krzeminski.snakeyaml.engine.kmp.events.ScalarEvent
import it.krzeminski.snakeyaml.engine.kmp.events.StreamStartEvent
import it.krzeminski.snakeyaml.engine.kmp.internal.utils.Character

class EmitterTest: FunSpec({
    test("write folded") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.FOLDED)
            .build()
        val folded = "0123456789 0123456789\n0123456789 0123456789"
        val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla\n",
        )
        val output = dump(settings, map)
        val expected = """"aaa": >-
  0123456789 0123456789

  0123456789 0123456789
"bbb": >2

  bla-bla
"""
        output shouldBe expected
    }

    test("write literal") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.LITERAL)
            .build()
        val folded = "0123456789 0123456789 0123456789 0123456789"
        val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla\n",
        )
        val output = dump(settings, map)
        val expected = """"aaa": |-
  0123456789 0123456789 0123456789 0123456789
"bbb": |2

  bla-bla
"""
        output shouldBe expected
    }

    test("write plain") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .build()
        val folded = "0123456789 0123456789\n0123456789 0123456789"
        val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla",
        )
        val output = dump(settings, map)
        val expected = """aaa: |-
  0123456789 0123456789
  0123456789 0123456789
bbb: |2-

  bla-bla
"""
        output shouldBe expected
    }

    test("write plain pretty") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setMultiLineFlow(true)
            .build()
        val folded = "0123456789 0123456789\n0123456789 0123456789"
       val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla",
       )
            val output = dump(settings, map)
        val expected = """aaa: |-
  0123456789 0123456789
  0123456789 0123456789
bbb: |2-

  bla-bla
"""
        output shouldBe expected
    }


    test("write single quoted") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.SINGLE_QUOTED)
            .build()
        val folded = "0123456789 0123456789\n0123456789 0123456789"
        val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla",
        )
        val output = dump(settings, map)
        val expected = """'aaa': '0123456789 0123456789

  0123456789 0123456789'
'bbb': '

  bla-bla'
"""
        output shouldBe expected
    }

    test("write double quoted") {
        val settings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .build()
        val folded = "0123456789 0123456789\n0123456789 0123456789"
        val map = mapOf(
            "aaa" to folded,
            "bbb" to "\nbla-bla"
        )
        val output = dump(settings, map)
        val expected = """"aaa": "0123456789 0123456789\n0123456789 0123456789"
"bbb": "\nbla-bla"
"""
        output shouldBe expected
    }

    test("write supplementary unicode") {
        val settings = DumpSettings.builder()
            .setUseUnicodeEncoding(false)
            .build()
        val burger = Character.toChars(0x1f354).concatToString()
        val halfBurger = "\uD83C"
        val output = StringStreamDataWriter()
        val emitter = Emitter(settings, output)

        emitter.emit(StreamStartEvent(null, null))
        emitter.emit(DocumentStartEvent(false, null, emptyMap(), null, null))
        emitter.emit(ScalarEvent(null, null, ImplicitTuple(true, false), burger + halfBurger, ScalarStyle.DOUBLE_QUOTED, null, null))
        val expected = "! \"\\U0001f354\\ud83c\""
        output.toString() shouldBe expected
    }

    test("split line, expect first flow sequence item") {
        val builder = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setWidth(8)
        val map = mapOf(
            "12345" to listOf("1111111111"),
        )

        // Split lines enabled (default)
        val output = dump(builder.build(), map)
        output shouldBe """{"12345": [
    "1111111111"]}
"""

        // Split lines disabled
        val output2 = dump(builder.setSplitLines(false).build(), map)
        output2 shouldBe """{"12345": ["1111111111"]}
"""
    }

    test("write indicator indent") {
        val settings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(5)
            .setIndicatorIndent(2)
            .build()
        val topLevel = listOf(mapOf("k1" to "v1"), mapOf("k2" to "v2"))
        val map = mapOf("aaa" to topLevel)
        val output = dump(settings, map)
        val expected = """aaa:
  -  k1: v1
  -  k2: v2
"""
        output shouldBe expected
    }

    test("split line, expect flow sequence item") {
        val builder = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setWidth(8)
        // Split lines enabled (default)
        val yaml1 = Dump(builder.build())
        val output = yaml1.dumpToString(listOf("1111111111", "2222222222"))
        output shouldBe """["1111111111",
  "2222222222"]
"""
        val output2 = yaml1.dumpToString(listOf("1", "2"))
        output2 shouldBe """["1", "2"]
"""

        // Split lines disabled
        val yaml2 = Dump(builder.setSplitLines(false).build())
        val output3 = yaml2.dumpToString(listOf("1111111111", "2222222222"))
        output3 shouldBe """["1111111111", "2222222222"]
"""
        val output4 = yaml2.dumpToString(listOf("1", "2"))
        output4 shouldBe """["1", "2"]
"""
    }

    test("split line, expect first flow mapping key") {
        val builder = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setWidth(16)
        val nonSplitMap = mapOf("3" to "4")
        val nonSplitContainerMap = mapOf("1 2" to nonSplitMap)
        val splitMap = mapOf("3333333333" to "4444444444")
        val  splitContainerMap = mapOf("1111111111 2222222222" to splitMap)

        // Split lines enabled (default)
        val output = dump(builder.build(), splitContainerMap)
        output shouldBe """{"1111111111 2222222222": {
    "3333333333": "4444444444"}}
"""
        val output2 = dump(builder.build(), nonSplitContainerMap)
        output2 shouldBe """{"1 2": {"3": "4"}}
"""
        // Split lines disabled
        val noSplit = builder.setSplitLines(false).build()
        val output3 = dump(noSplit, splitContainerMap)
        output3 shouldBe """{"1111111111 2222222222": {"3333333333": "4444444444"}}
"""
        val output4 = dump(noSplit, nonSplitContainerMap)
        output4 shouldBe """{"1 2": {"3": "4"}}
"""
    }

    test("split line, expect flow mapping key") {
        val builder = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setWidth(16)
        val nonSplitMap = mapOf(
            "1" to "2",
            "3" to "4",
        )
        val splitMap = mapOf(
            "1111111111" to "2222222222",
            "3333333333" to "4444444444",
        )

        // Split lines enabled (default)
        val output = dump(builder.build(), splitMap)
        output shouldBe """{"1111111111": "2222222222",
  "3333333333": "4444444444"}
"""
        val output2 = dump(builder.build(), nonSplitMap)
        output2 shouldBe """{"1": "2", "3": "4"}
"""

        // Split lines disabled
        val noSplit = builder.setSplitLines(false).build()
        val output3 = dump(noSplit, splitMap)
        output3  shouldBe """{"1111111111": "2222222222", "3333333333": "4444444444"}
"""
        val output4 = dump(noSplit, nonSplitMap)
        output4 shouldBe """{"1": "2", "3": "4"}
"""
    }

    test("anchor in maps") {
        val builder = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.FLOW)
        var map1 = mutableMapOf<String, Any>()
        var map2 = mutableMapOf<String, Any>()
        map1.put("2", map2)
        map2.put("1", map1)
        val output = dump(builder.build(), map1)
        output shouldBe "&id001 {'2': {'1': *id001}}\n"
    }


    test("expected space to separate alias from colon") {
        val builder = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.FLOW)
        // this is VERY BAD code
        // the map has itself as a key (no idea why it may be used except of a DoS attack)
        val f = mutableMapOf<Any, Any>()
        f.put(f, "a")

        val output = dump(builder.build(), f)
        output shouldBe "&id001 {*id001 : a}\n"
        val load = Load(LoadSettings.builder()
            .setAllowRecursiveKeys(true)
            .build())
        val obj = load.loadOne(output)
        obj.shouldNotBeNull()
    }

    test("indicator indent range") {
        withClue("indicator indent range start must be non-negative") {
            Emitter.VALID_INDICATOR_INDENT_RANGE.start shouldBeGreaterThanOrEqualTo 0
        }
        withClue("indicator indent range start must be one less than the indent range start") {
            Emitter.VALID_INDICATOR_INDENT_RANGE.start shouldBe Emitter.VALID_INDENT_RANGE.start - 1
        }
        withClue("indicator indent range end must be one less than the indent range end") {
            Emitter.VALID_INDICATOR_INDENT_RANGE.endInclusive shouldBe Emitter.VALID_INDENT_RANGE.endInclusive - 1
        }
    }

    test("allow setting big width") {
        val builder = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setWidth(1000)
        val map = mapOf(
            "12345" to "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 " +
            "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 " +
            "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890"
        )

        val output = dump(builder.build(), map)
        output shouldBe "{\"12345\": \"1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 " +
            "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 " +
            "1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890 1234567890\"}\n"
    }
})

private fun dump(settings: DumpSettings, map: Any?): String =
    Dump(settings).dumpToString(map)
