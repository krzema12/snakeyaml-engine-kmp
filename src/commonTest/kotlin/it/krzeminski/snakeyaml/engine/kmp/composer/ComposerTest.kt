package it.krzeminski.snakeyaml.engine.kmp.composer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ComposerException

class ComposerTest : FunSpec({

    test("fail to compose one document when more documents are provided.") {
        shouldThrow<ComposerException> {
            Compose(LoadSettings.builder().build()).compose("a\n---\nb\n")
        }.also {
            it.message shouldContain "expected a single document in the stream"
            it.message shouldContain "but found another document"
        }
    }

    test("fail to compose unknown alias") {
        shouldThrow<ComposerException> {
            Compose(LoadSettings.builder().build()).compose("[a, *id b]")
        }.also {
            it.message shouldContain "found undefined alias id"
        }
    }

    test("compose anchor") {
        val data = "--- &113\n{name: Bill, age: 18}"
        val compose = Compose(LoadSettings.builder().build())
        val optionalNode = compose.compose(data)
        optionalNode.shouldNotBeNull()
        optionalNode.anchor!!.value shouldBe "113"
    }
})
