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
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

class ComposerTest : FunSpec({
    fun compose(data: String): Node = Compose(LoadSettings()).compose(data)!!

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

    test("compose anchor") {
        val data = "--- &113\n{name: Bill, age: 18}"
        val compose = Compose(LoadSettings())
        val optionalNode = compose.compose(data)
        optionalNode.shouldNotBeNull()
        optionalNode.anchor!!.value shouldBe "113"
    }

    test("fail to compose non scalar key") {
        val exception = shouldThrow<YamlEngineException> {
            Compose(LoadSettings()).compose("{ [1,2]: value}")
        }
        exception.message shouldBe "Non scalar key is detected but it is not configured to be allowed."
    }

    test("compose non scalar key when allowed") {
        val node = Compose(LoadSettings(allowNonScalarKeys = true)).compose("{ [1,2]: value}")
        node.shouldNotBeNull()
    }

    test("A tag which is not in the source is resolved.") {
        compose("18").isResolved() shouldBe true
        compose("[a]").isResolved() shouldBe true
        compose("{a: b}").isResolved() shouldBe true
    }

    test("A tag which is in the source is not resolved.") {
        compose("!!str 18").also { it.isResolved() shouldBe false; it.tag shouldBe Tag.STR }
        compose("!!seq [a]").also { it.isResolved() shouldBe false; it.tag shouldBe Tag.SEQ }
        compose("!!map {a: b}").also { it.isResolved() shouldBe false; it.tag shouldBe Tag.MAP }
    }

    test("The non-specific tag is resolved as well.") {
        compose("! 18").isResolved() shouldBe true
        compose("! [a]").isResolved() shouldBe true
        compose("! {a: b}").isResolved() shouldBe true
    }
})
