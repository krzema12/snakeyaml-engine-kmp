package it.krzeminski.snakeyaml.engine.kmp.composer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ComposerException
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

class ComposerTest : FunSpec({

    test("fail to compose one document when more documents are provided.") {
        shouldThrow<ComposerException> {
            Compose(LoadSettings()).compose("a\n---\nb\n")
        }.also {
            it.message shouldContain "expected a single document in the stream"
            it.message shouldContain "but found another document"
        }
    }

    test("fail to compose unknown alias") {
        shouldThrow<ComposerException> {
            Compose(LoadSettings()).compose("[a, *id b]")
        }.also {
            it.message shouldContain "found undefined alias id"
        }
    }

    test("fail to compose non-scalar key") {
        shouldThrow<YamlEngineException> {
            Compose(LoadSettings(allowNonScalarKeys = false)).compose("{ [1,2]: value}")
        }.also {
            it.message shouldBe "Non scalar key is detected but it is not configured to be allowed."
        }
    }

    test("compose anchor") {
        val data = "--- &113\n{name: Bill, age: 18}"
        val compose = Compose(LoadSettings())
        val optionalNode = compose.compose(data)
        optionalNode.shouldNotBeNull()
        optionalNode.anchor!!.value shouldBe "113"
    }
})
