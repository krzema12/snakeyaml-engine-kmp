package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings.SpecVersionMutator
import it.krzeminski.snakeyaml.engine.kmp.exceptions.DuplicateKeyException
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class LoadSettingsTest : FunSpec({
    test("accept only YAML 1.2") {
        val strict12 = SpecVersionMutator { t ->
            if (t.major != 1 || t.minor != 2) {
                throw IllegalArgumentException("Only 1.2 is supported.")
            } else {
                t
            }
        }

        val settings = LoadSettings.builder().setVersionFunction(strict12).build()
        val load = Load(settings)
        shouldThrowWithMessage<IllegalArgumentException>(message = "Only 1.2 is supported.") {
            load.loadOne("%YAML 1.1\n...\nfoo")
        }
    }

    test("do not allow duplicate keys") {
        val settings = LoadSettings.builder().setAllowDuplicateKeys(false).build()
        val load = Load(settings)
        shouldThrow<DuplicateKeyException> {
            load.loadOne("{a: 1, a: 2}")
        }.also {
            it.message shouldContain "found duplicate key a"
        }
    }

    test("do not allow duplicate keys by default") {
        val load = Load()
        shouldThrow<DuplicateKeyException> {
            load.loadOne("{a: 1, a: 2}")
        }.also {
            it.message shouldContain "found duplicate key a"
        }
    }

    test("allow duplicate keys") {
        val settings = LoadSettings.builder().setAllowDuplicateKeys(true).build()
        val load = Load(settings)
        val map = load.loadOne("{a: 1, a: 2}") as Map<String, Int>
        map["a"] shouldBe 2
    }

    test("set and get custom property") {
        val key = SomeKey()
        val settings = LoadSettings.builder()
            .setCustomProperty(key, "foo")
            .setCustomProperty(SomeStatus.DELIVERED, "bar")
            .build()
        settings.getCustomProperty(key) shouldBe "foo"
        settings.getCustomProperty(SomeStatus.DELIVERED) shouldBe "bar"
    }

    test("set and get custom I/O buffer size") {
        val settings = LoadSettings.builder().setBufferSize(4096).build()
        settings.bufferSize shouldBe 4096
    }

    test("use Core schema by default") {
        val settings = LoadSettings.builder().build()
        settings.schema::class shouldBe CoreSchema::class
    }
})

private enum class SomeStatus : SettingKey {
    ORDERED, DELIVERED
}

private class SomeKey : SettingKey
