package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.NonPrintableStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.common.SpecVersion
import it.krzeminski.snakeyaml.engine.kmp.exceptions.EmitterException
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class DumpSettingsTest : FunSpec({

    test("check default values") {
        val settings = DumpSettings()

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
        val settings = DumpSettings(isCanonical = true)
        val dump = Dump(settings)
        val data = listOf(0, 1)
        val str = dump.dumpToString(data)
        str shouldBe "---\n!!seq [\n  !!int \"0\",\n  !!int \"1\",\n]\n"
    }

    test("use Windows line break") {
        val settings = DumpSettings(bestLineBreak = "\r\n")
        val dump = Dump(settings)
        val data = listOf(0, 1)
        val str = dump.dumpToString(data)
        str shouldBe "[0, 1]\r\n"
    }

    test("setMultiLineFlow") {
        val settings = DumpSettings(isMultiLineFlow = true)
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
        val settings = DumpSettings(tagDirective = tagDirectives)
        val dump = Dump(settings)
        val str = dump.dumpToString("data")
        str shouldBe "%TAG !python! !python\n%TAG !yaml! tag:yaml.org,2002:\n--- data\n"
    }

    test("check corner cases for indent") {
        shouldThrowWithMessage<EmitterException>(message = "Indent must be at in range 1..10") {
            DumpSettings(indent = 0)
        }

        shouldThrowWithMessage<EmitterException>(message = "Indent must be at in range 1..10") {
            DumpSettings(indent = 11)
        }
    }

    test("check corner cases for Indicator Indent") {
        shouldThrowWithMessage<EmitterException>(message = "Indicator indent must be in range 0..9") {
            DumpSettings(indicatorIndent = -1)
        }

        shouldThrowWithMessage<EmitterException>(message = "Indicator indent must be in range 0..9") {
            DumpSettings(indicatorIndent = 10)
        }
    }

    test("dump explicit version") {
        val settings = DumpSettings(yamlDirective = SpecVersion(1, 2))
        val dump = Dump(settings)
        val str = dump.dumpToString("a")
        str shouldBe "%YAML 1.2\n--- a\n"
    }

    test("dump custom property") {
        val settings = DumpSettings(customProperties = mapOf(KeyName("key") to "value"))
        settings.customProperties[KeyName("key")] shouldBe "value"
        settings.customProperties[KeyName("None")].shouldBeNull()
    }

    test("use Core schema by default") {
        val settings = DumpSettings()
        settings.schema.shouldBeInstanceOf<CoreSchema>()
    }

    test("copy DSL smoke test") {
        val settings1 = DumpSettings()
        val settings2 = settings1.copy {
            width = 12345
        }
        settings1.width shouldNotBe settings2.width
    }
})

private data class KeyName(val keyName: String) : SettingKey
