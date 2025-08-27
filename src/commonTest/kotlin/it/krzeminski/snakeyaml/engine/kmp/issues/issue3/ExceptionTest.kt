package it.krzeminski.snakeyaml.engine.kmp.issues.issue3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class ExceptionTest : FunSpec({
    test("Sequence exception") {
        val load = Load()
        val exception = shouldThrow<YamlEngineException> {
            load.loadOne("!!seq abc")
        }
        exception.message shouldContain "java.lang.ClassCastException"
        exception.message shouldContain "it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode"
        exception.message shouldContain "cannot be cast to"
        exception.message shouldContain "it.krzeminski.snakeyaml.engine.kmp.nodes.SequenceNode"
    }

    test("Int exception") {
        val load = Load()
        val exception = shouldThrow<YamlEngineException> {
            load.loadOne("!!int abc")
        }
        exception.message shouldBe "java.lang.NumberFormatException: For input string: \"abc\""
    }
})
