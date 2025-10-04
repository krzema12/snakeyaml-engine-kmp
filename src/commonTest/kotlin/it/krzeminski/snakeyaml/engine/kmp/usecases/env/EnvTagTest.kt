package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.api.lowlevel.Compose
import it.krzeminski.snakeyaml.engine.kmp.nodes.Tag

/**
 * test that implicit resolver assigns the tag
 */
class EnvTagTest : FunSpec({
    test("implicit resolver for env constructor") {
        val loader = Compose(LoadSettings())
        val loaded = loader.compose($$"${PATH}")
        loaded?.tag shouldBe Tag.ENV_TAG
    }
})
