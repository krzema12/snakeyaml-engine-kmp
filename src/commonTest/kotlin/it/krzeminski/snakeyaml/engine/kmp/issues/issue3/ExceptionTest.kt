package it.krzeminski.snakeyaml.engine.kmp.issues.issue3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class ExceptionTest : FunSpec({
    test("sequence exception") {
        val load = Load()
        shouldThrow<YamlEngineException> {
            load.loadOne("!!seq abc")
        }.also {
            it.message shouldContain "ClassCastException"
        }
    }

    test("int exception") {
        val load = Load()
        val operationUnderTest = { load.loadOne("!!int abc") }

        if (platform == Platform.JVM) {
            shouldThrowWithMessage<YamlEngineException>(message = "java.lang.NumberFormatException: For input string: \"abc\"") {
                operationUnderTest()
            }
        } else {
            // TODO: https://github.com/krzema12/snakeyaml-engine-kmp/issues/49
            shouldThrow<NotImplementedError> {
                operationUnderTest()
            }.also {
                it.message shouldContain "An operation is not implemented: Kotlin/JS BigInteger implementation"
            }
        }
    }
})
