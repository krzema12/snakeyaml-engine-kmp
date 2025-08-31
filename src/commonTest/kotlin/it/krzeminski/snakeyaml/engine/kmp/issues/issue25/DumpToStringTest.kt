package it.krzeminski.snakeyaml.engine.kmp.issues.issue25

import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class DumpToStringTest : FunSpec({
    test("if Dump instance is called more then once then the results are not predictable") {
        val dumpSettings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()
        val dump = Dump(dumpSettings)

        class Something {
            @Suppress("UNUSED")
            val doesntmatter = 0
        }

        val something = Something()
        val data = mapOf(
            "before" to "bla",
            "nested" to something,
        )

        shouldThrowWithMessage<YamlEngineException>(message = "Representer is not defined for class Something") {
            dump.dumpToString(data)
        }

        val output = dump.dumpToString(data)
        output shouldBe "before: bla\n"
    }
})
