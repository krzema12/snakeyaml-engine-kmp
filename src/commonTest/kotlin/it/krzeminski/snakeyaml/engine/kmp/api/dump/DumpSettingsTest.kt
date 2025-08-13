package it.krzeminski.snakeyaml.engine.kmp.api.dump

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
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

    test("check default values") {
        val settings = DumpSettings.builder().build()

        with(settings) {
            assertSoftly {
                bestLineBreak shouldBe "\n"
                indent shouldBe 2
                defaultFlowStyle shouldBe FlowStyle.AUTO
                defaultScalarStyle shouldBe ScalarStyle.PLAIN
                explicitRootTag shouldBe null
                indentWithIndicator shouldBe false
                isExplicitEnd shouldBe false
                isExplicitStart shouldBe false
                isCanonical shouldBe false
                isSplitLines shouldBe true
                isMultiLineFlow shouldBe false
                isUseUnicodeEncoding shouldBe true
                indicatorIndent shouldBe 0
                maxSimpleKeyLength shouldBe 128
                nonPrintableStyle shouldBe NonPrintableStyle.ESCAPE
                width shouldBe 80
                yamlDirective shouldBe null
                tagDirective shouldBe emptyMap()
                anchorGenerator.shouldNotBeNull()
            }
        }
    }

    test("canonical output") {
        val settings = DumpSettings.builder().setCanonical(true).build()
        val dump = Dump(settings)
        val data = listOf(0, 1)
        val str = dump.dumpToString(data)
        str shouldBe "---\n!!seq [\n  !!int \"0\",\n  !!int \"1\",\n]\n"
    }

    test("use Windows line break") {
        val settings = DumpSettings.builder().setBestLineBreak("\r\n").build()
        val dump = Dump(settings)
        val data = listOf(0, 1)
        val str = dump.dumpToString(data)
        str shouldBe "[0, 1]\r\n"
    }

    test("setMultiLineFlow") {
        val settings = DumpSettings.builder().setMultiLineFlow(true).build()
        val dump = Dump(settings)
        val data = listOf(0, 1, 2)
        val str = dump.dumpToString(data)
        str shouldBe "[\n  0,\n  1,\n  2\n]\n"
    }

    test("show tag directives") {
        val tagDirectives = mapOf(
            "!python!" to "!python",
            "!yaml!" to "tag:yaml.org,2002:"
        )
        val settings = DumpSettings.builder().setTagDirective(tagDirectives).build()
        val dump = Dump(settings)
        val str = dump.dumpToString("data")
        str shouldBe "%TAG !python! !python\n%TAG !yaml! tag:yaml.org,2002:\n--- data\n"
    }

    test("check corner cases for indent") {
        shouldThrowWithMessage<EmitterException>(message = "Indent must be at in range 1..10") {
            DumpSettings.builder().setIndent(0)
        }

        shouldThrowWithMessage<EmitterException>(message = "Indent must be at in range 1..10") {
            DumpSettings.builder().setIndent(11)
        }
    }

    test("check corner cases for Indicator Indent") {
        shouldThrowWithMessage<EmitterException>(message = "Indicator indent must be in range 0..9") {
            DumpSettings.builder().setIndicatorIndent(-1)
        }

        shouldThrowWithMessage<EmitterException>(message = "Indicator indent must be in range 0..9") {
            DumpSettings.builder().setIndicatorIndent(10)
        }
    }

    test("dump explicit version") {
        val settings = DumpSettings.builder().setYamlDirective(SpecVersion(1, 2)).build()
        val dump = Dump(settings)
        val str = dump.dumpToString("a")
        str shouldBe "%YAML 1.2\n--- a\n"
    }

    test("dump custom property") {
        val settings = DumpSettings.builder()
            .setCustomProperty(KeyName("key"), "value")
            .build()
        settings.getCustomProperty(KeyName("key")) shouldBe "value"
        settings.getCustomProperty(KeyName("None")).shouldBeNull()
    }

    test("use Core schema by default") {
        val settings = DumpSettings.builder().build()
        settings.schema.shouldBeInstanceOf<CoreSchema>()
    }
})

private data class KeyName(val keyName: String) : SettingKey
