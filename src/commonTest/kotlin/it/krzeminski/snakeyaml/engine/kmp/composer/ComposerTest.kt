package it.krzeminski.snakeyaml.engine.kmp.composer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ComposerException

class ComposerTest : FunSpec({

    test("Fail to Compose one document when more documents are provided.") {
        val exception = shouldThrow<ComposerException> {
            Compose(LoadSettings.builder().build()).compose("a\n---\nb\n")
        }
        exception.message shouldContain "expected a single document in the stream"
        exception.message shouldContain "but found another document"
    }

    test("failToComposeUnknownAlias") {
        val exception = shouldThrow<ComposerException> {
            Compose(LoadSettings.builder().build()).compose("[a, *id b]")
        }
        exception.message shouldContain "found undefined alias id"
    }

    test("composeAnchor") {
        val data = "--- &113\n{name: Bill, age: 18}"
        val compose = Compose(LoadSettings.builder().build())
        val optionalNode = compose.compose(data)
        optionalNode shouldNotBe null
        optionalNode!!.anchor!!.value shouldBe "113"
    }
})
