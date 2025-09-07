package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrowWithMessage
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlVersionException
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode

class SpecVersionTest : FunSpec({

    test("version 1.2 is accepted") {
        val settings = LoadSettings.builder().setLabel("spec 1.2").build()
        val node = Compose(settings).compose("%YAML 1.2\n---\nfoo") as ScalarNode
        node.value shouldBe "foo"
    }

    test("version 1.3 is accepted by default") {
        val settings = LoadSettings.builder().setLabel("spec 1.3").build()
        val node = Compose(settings).compose("%YAML 1.3\n---\nfoo") as ScalarNode
        node.value shouldBe "foo"
    }

    test("version 1.3 is rejected if configured") {
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
        shouldThrowWithMessage<IllegalArgumentException>(message = "Too high.") {
            Compose(settings).compose("%YAML 1.3\n---\nfoo")
        }
    }

    test("version 2.0 is rejected") {
        val settings = LoadSettings.builder().setLabel("spec 2.0").build()
        shouldThrowWithMessage<YamlVersionException>(message = "Version{major=2, minor=0}") {
            Compose(settings).compose("%YAML 2.0\n---\nfoo")
        }
    }
})
