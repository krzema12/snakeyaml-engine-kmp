package it.krzeminski.snakeyaml.engine.kmp.issues.issue25

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class DumpToStringTest : FunSpec({
    test("If Dump instance is called more then once then the results are not predictable") {
        val data = linkedMapOf<String, Any>()
        val dumpSettings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build()
        val dump = Dump(dumpSettings)

        class Something {
            @Suppress("UNUSED")
            val doesntmatter = 0
        }

        val something = Something()
        data["before"] = "bla"
        data["nested"] = something

        val exception = shouldThrow<YamlEngineException> {
            dump.dumpToString(data)
        }
        exception.message shouldBe "Representer is not defined for class Something"

        val output = dump.dumpToString(data)
        output shouldBe "before: bla\n"
    }
})
