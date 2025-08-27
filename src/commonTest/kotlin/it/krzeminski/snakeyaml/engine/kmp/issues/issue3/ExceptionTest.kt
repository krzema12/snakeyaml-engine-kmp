package it.krzeminski.snakeyaml.engine.kmp.issues.issue3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.Platform
import io.kotest.common.platform
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
        exception.message shouldContain "ClassCastException"
    }

    test("Int exception") {
        val load = Load()
        val operationUnderTest = { load.loadOne("!!int abc") }
        if (platform == Platform.JVM) {
            val exception = shouldThrow<YamlEngineException> {
                operationUnderTest()
            }
            exception.message shouldBe "java.lang.NumberFormatException: For input string: \"abc\""
        } else {
            // TODO: https://github.com/krzema12/snakeyaml-engine-kmp/issues/49
            val exception = shouldThrow<NotImplementedError> {
                operationUnderTest()
            }
            exception.message shouldContain "An operation is not implemented: Kotlin/JS BigInteger implementation"
        }
    }
})
