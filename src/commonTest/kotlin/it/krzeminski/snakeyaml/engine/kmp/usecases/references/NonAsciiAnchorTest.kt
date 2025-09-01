package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load

class NonAsciiAnchorTest : FunSpec({
    test("non ASCII anchor name must be accepted") {
        val load = Load()
        val floatValue = load.loadOne("&something_タスク タスク") as String
        floatValue shouldBe "タスク"
    }

    test("underscore") {
        val loader = Load()
        val value = loader.loadOne("&_ タスク")
        value shouldBe "タスク"
    }

    test("smile") {
        val loader = Load()
        val value = loader.loadOne("&\uD83D\uDE01 v1")
        value shouldBe "v1"
    }

    test("alpha") {
        val loader = Load()
        val value = loader.loadOne("&kääk v1")
        value shouldBe "v1"
    }

    test("reject invalid anchors which contain special characters") {
        val nonAnchors = ",[]{}*&./"
        for (i in nonAnchors.indices) {
            shouldThrow<Exception> {
                loadWith(nonAnchors[i])
            }.also { exception ->
                exception.message shouldContain "while scanning an anchor"
                exception.message shouldContain "unexpected character found"
            }
        }
    }
})

private fun loadWith(c: Char) {
    val load = Load()
    load.loadOne("&$c value")
}
