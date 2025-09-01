package it.krzeminski.snakeyaml.engine.kmp.usecases.untrusted

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class ByteLimitTest : FunSpec({
    test("limit a single document") {
        val settings = LoadSettings.builder().setCodePointLimit(15).build()
        val load = Load(settings)
        shouldThrow<Exception> {
            load.loadOne("12345678901234567890")
        }.also { exception ->
            exception.message shouldBe "The incoming YAML document exceeds the limit: 15 code points."
        }
    }

    test("load all 553") {
        val settings = LoadSettings.builder().setCodePointLimit(15).build()
        val load = Load(settings)
        shouldThrow<Exception> {
            val iter = load.loadAll("12345678901234567890").iterator()
            iter.next()
        }.also { exception ->
            exception.message shouldBe "The incoming YAML document exceeds the limit: 15 code points."
        }
    }

    test("load many documents") {
        val settings = LoadSettings.builder().setCodePointLimit(8).build()
        val load = Load(settings)
        val iter = load.loadAll("---\nfoo\n---\nbar\n---\nyep").iterator()
        iter.next() shouldBe "foo"
        iter.next() shouldBe "bar"
        iter.next() shouldBe "yep"
        iter.hasNext() shouldBe false
    }
})
