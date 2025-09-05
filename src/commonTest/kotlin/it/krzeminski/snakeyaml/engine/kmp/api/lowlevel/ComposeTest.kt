package it.krzeminski.snakeyaml.engine.kmp.api.lowlevel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import okio.Buffer

class ComposeTest : FunSpec({
    test("compose empty string") {
        val compose = Compose(LoadSettings.builder().build())
        val node = compose.compose("")
        node shouldBe null
    }

    test("compose empty buffer") {
        val compose = Compose(LoadSettings.builder().build())
        val node = compose.compose(Buffer())
        node shouldBe null
    }

    test("compose all from empty string") {
        val compose = Compose(LoadSettings.builder().build())
        val nodes = compose.composeAll("")
        nodes.iterator().hasNext() shouldBe false
    }

    test("compose all from empty buffer") {
        val compose = Compose(LoadSettings.builder().build())
        val nodes = compose.composeAll(Buffer())
        nodes.iterator().hasNext() shouldBe false
    }
})
