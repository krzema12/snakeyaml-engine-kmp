package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings.SpecVersionMutator
import it.krzeminski.snakeyaml.engine.kmp.exceptions.DuplicateKeyException
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class LoadSettingsTest : FunSpec({
    test("Accept only YAML 1.2") {
        val strict12 = SpecVersionMutator { t ->
            if (t.major != 1 || t.minor != 2) {
                throw IllegalArgumentException("Only 1.2 is supported.")
            } else {
                t
            }
        }

        val settings = LoadSettings.builder().setVersionFunction(strict12).build()
        val load = Load(settings)
        val ex = shouldThrow<IllegalArgumentException> {
            load.loadOne("%YAML 1.1\n...\nfoo")
        }
        ex.message shouldBe "Only 1.2 is supported."
    }

    test("Do not allow duplicate keys") {
        val settings = LoadSettings.builder().setAllowDuplicateKeys(false).build()
        val load = Load(settings)
        val ex = shouldThrow<DuplicateKeyException> {
            load.loadOne("{a: 1, a: 2}")
        }
        ex.message!! shouldContain "found duplicate key a"
    }

    test("Do not allow duplicate keys by default") {
        val load = Load()
        val ex = shouldThrow<DuplicateKeyException> {
            load.loadOne("{a: 1, a: 2}")
        }
        ex.message!! shouldContain "found duplicate key a"
    }

    test("Allow duplicate keys") {
        val settings = LoadSettings.builder().setAllowDuplicateKeys(true).build()
        val load = Load(settings)
        val map = load.loadOne("{a: 1, a: 2}") as Map<String, Int>
        map["a"] shouldBe 2
    }

    test("Set and get custom property") {
        val key = SomeKey()
        val settings = LoadSettings.builder()
            .setCustomProperty(key, "foo")
            .setCustomProperty(SomeStatus.DELIVERED, "bar")
            .build()
        settings.getCustomProperty(key) shouldBe "foo"
        settings.getCustomProperty(SomeStatus.DELIVERED) shouldBe "bar"
    }

    test("Set and get custom I/O buffer size") {
        val settings = LoadSettings.builder().setBufferSize(4096).build()
        settings.bufferSize shouldBe 4096
    }

    test("Use Core schema by default") {
        val settings = LoadSettings.builder().build()
        settings.schema::class shouldBe CoreSchema::class
    }
})

private enum class SomeStatus : SettingKey {
    ORDERED, DELIVERED
}

private class SomeKey : SettingKey
