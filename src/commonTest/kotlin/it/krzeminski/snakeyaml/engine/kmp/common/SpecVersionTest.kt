package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlVersionException
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode

class SpecVersionTest : FunSpec({

    test("Version 1.2 is accepted") {
        val settings = LoadSettings.builder().setLabel("spec 1.2").build()
        val node = Compose(settings).compose("%YAML 1.2\n---\nfoo") as ScalarNode
        node.value shouldBe "foo"
    }

    test("Version 1.3 is accepted by default") {
        val settings = LoadSettings.builder().setLabel("spec 1.3").build()
        val node = Compose(settings).compose("%YAML 1.3\n---\nfoo") as ScalarNode
        node.value shouldBe "foo"
    }

    test("Version 1.3 is rejected if configured") {
        val settings = LoadSettings.builder()
            .setLabel("spec 1.3")
            .setVersionFunction { version ->
                if (version.minor > 2) {
                    throw IllegalArgumentException("Too high.")
                } else {
                    version
                }
            }
            .build()
        val exception = shouldThrow<IllegalArgumentException> {
            Compose(settings).compose("%YAML 1.3\n---\nfoo")
        }
        exception.message shouldBe "Too high."
    }

    test("Version 2.0 is rejected") {
        val settings = LoadSettings.builder().setLabel("spec 2.0").build()
        val exception = shouldThrow<YamlVersionException> {
            Compose(settings).compose("%YAML 2.0\n---\nfoo")
        }
        exception.message shouldBe "Version{major=2, minor=0}"
    }
})
