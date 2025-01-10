package it.krzeminski.snakeyaml.engine.kmp.usecases.indentation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class IndentWithIndicatorTest : FunSpec({
    test("indent with indicator 1") {
        val settings: DumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(true)
            .setIndent(2)
            .setIndicatorIndent(1)
            .build()
        val dumper = Dump(settings)
        val output = dumper.dumpToString(DATA)

        val doc = stringFromResources("/indentation/issue416-1.yaml")
        output shouldBe doc
    }

    test("indent with indicator 2") {
        val settings: DumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(true)
            .setIndent(2)
            .setIndicatorIndent(2)
            .build()
        val dumper = Dump(settings)
        val output = dumper.dumpToString(DATA)

        val doc = stringFromResources("/indentation/issue416-2.yaml")
        output shouldBe doc
    }

    test("indent with indicator 3") {
        val settings: DumpSettings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndentWithIndicator(false)
            .setIndent(4)
            .setIndicatorIndent(2)
            .build()
        val dumper = Dump(settings)
        val output = dumper.dumpToString(DATA)

        val doc = stringFromResources("/indentation/issue416-3.yaml")
        output shouldBe doc
    }
})

private val DATA = mapOf(
    "company" to mapOf(
        "developers" to listOf(
            mapOf(
                "name" to "Fred",
                "role" to "creator",
            ),
            mapOf(
                "name" to "John",
                "role" to "committer",
            ),
        ),
        "name" to "Yet Another Company",
        "location" to "Maastricht",
    )
)
