package it.krzeminski.snakeyaml.engine.kmp.usecases.json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

@Suppress("UNCHECKED_CAST")
class ParseJsonTest : FunSpec({
    test("Parse JSON with TABs") {
        val obj = Load(LoadSettings()).loadOne(stringFromResources("/json/mtad.yaml")) as Map<String, Any?>
        obj.size shouldBe 4
        obj.containsKey("_schema-version") shouldBe true
    }

    test("Parse JSON with TABs, small") {
        val obj = Load(LoadSettings()).loadOne(stringFromResources("/json/leading-tab.yaml")) as Map<String, Any?>
        obj.size shouldBe 3
        obj.containsKey("modules") shouldBe true
    }
})
