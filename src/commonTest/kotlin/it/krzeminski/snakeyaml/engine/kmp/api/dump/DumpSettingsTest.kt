package it.krzeminski.snakeyaml.engine.kmp.api.dump

import io.kotest.core.spec.style.FunSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.SettingKey
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class DumpSettingsTest : FunSpec({

    test("Check default values") {
        val settings = DumpSettings.builder().build()

        settings.bestLineBreak shouldBe "\n"
        settings.indent shouldBe 2
        settings.defaultFlowStyle shouldBe FlowStyle.AUTO
        settings.defaultScalarStyle shouldBe ScalarStyle.PLAIN
        settings.explicitRootTag shouldBe null
        settings.indentWithIndicator shouldBe false
        settings.isExplicitEnd shouldBe false
        settings.isExplicitStart shouldBe false
        settings.isCanonical shouldBe false
        settings.isSplitLines shouldBe true
        settings.isMultiLineFlow shouldBe false
        settings.isUseUnicodeEncoding shouldBe true
        settings.indicatorIndent shouldBe 0
        settings.maxSimpleKeyLength shouldBe 128
        settings.nonPrintableStyle shouldBe NonPrintableStyle.ESCAPE
        settings.width shouldBe 80
        settings.yamlDirective shouldBe null
        settings.tagDirective shouldBe emptyMap()
        settings.anchorGenerator.shouldNotBeNull()
    }

    test("Canonical output") {
        val settings = DumpSettings.builder().setCanonical(true).build()
        val dump = Dump(settings)
        val data = buildList {
            for (i in 0 until 2) add(i)
        }
        val str = dump.dumpToString(data)
        str shouldBe "---\n!!seq [\n  !!int \"0\",\n  !!int \"1\",\n]\n"
    }

    test("Use Windows line break") {
        val settings = DumpSettings.builder().setBestLineBreak("\r\n").build()
        val dump = Dump(settings)
        val data = buildList {
            for (i in 0 until 2) add(i)
        }
        val str = dump.dumpToString(data)
        str shouldBe "[0, 1]\r\n"
    }

    test("setMultiLineFlow") {
        val settings = DumpSettings.builder().setMultiLineFlow(true).build()
        val dump = Dump(settings)
        val data = buildList {
            for (i in 0 until 3) add(i)
        }
        val str = dump.dumpToString(data)
        str shouldBe "[\n  0,\n  1,\n  2\n]\n"
    }

    test("Show tag directives") {
        val tagDirectives = linkedMapOf(
            "!python!" to "!python",
            "!yaml!" to "tag:yaml.org,2002:"
        )
        val settings = DumpSettings.builder().setTagDirective(tagDirectives).build()
        val dump = Dump(settings)
        val str = dump.dumpToString("data")
        str shouldBe "%TAG !python! !python\n%TAG !yaml! tag:yaml.org,2002:\n--- data\n"
    }

    test("Check corner cases for indent") {
        val exception1 = shouldThrow<EmitterException> {
            DumpSettings.builder().setIndent(0)
        }
        exception1.message shouldBe "Indent must be at in range 1..10"

        val exception2 = shouldThrow<EmitterException> {
            DumpSettings.builder().setIndent(11)
        }
        exception2.message shouldBe "Indent must be at in range 1..10"
    }

    test("Check corner cases for Indicator Indent") {
        val exception1 = shouldThrow<EmitterException> {
            DumpSettings.builder().setIndicatorIndent(-1)
        }
        exception1.message shouldBe "Indicator indent must be in range 0..9"

        val exception2 = shouldThrow<EmitterException> {
            DumpSettings.builder().setIndicatorIndent(10)
        }
        exception2.message shouldBe "Indicator indent must be in range 0..9"
    }

    test("Dump explicit version") {
        val settings = DumpSettings.builder().setYamlDirective(SpecVersion(1, 2)).build()
        val dump = Dump(settings)
        val str = dump.dumpToString("a")
        str shouldBe "%YAML 1.2\n--- a\n"
    }

    test("dumpCustomProperty") {
        val settings = DumpSettings.builder()
            .setCustomProperty(KeyName("key"), "value")
            .build()
        settings.getCustomProperty(KeyName("key")) shouldBe "value"
        settings.getCustomProperty(KeyName("None")).shouldBeNull()
    }

    test("Use Core schema by default") {
        val settings = DumpSettings.builder().build()
        settings.schema::class shouldBe CoreSchema::class
    }
})

private data class KeyName(val keyName: String) : SettingKey
