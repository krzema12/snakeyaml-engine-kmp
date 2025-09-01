package it.krzeminski.snakeyaml.engine.kmp.usecases.colon_in_flow_context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load

class ColonInFlowContextInMapTest : FunSpec({
    test("with separation") {
        val loader = Load()
        val map = loader.loadOne("{a: 1}") as Map<String, Int>
        map["a"] shouldBe 1
    }

    test("without empty value") {
        val loader = Load()
        val map = loader.loadOne("{a:}") as Map<String, Any?>
        map.containsKey("a") shouldBe true
    }

    test("without separation") {
        val loader = Load()
        val map = loader.loadOne("{a:1}") as Map<String, Any>
        map.containsKey("a:1") shouldBe true
    }
})
