package it.krzeminski.snakeyaml.engine.kmp.issues.issue512

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class ListWithCommentTest: FunSpec({
    test("Issue 512 from SnakeYAML") {
        val str = stringFromResources("/comments/issue512.yaml")
        val options = LoadSettings(parseComments = true)
        val load = Load(options)
        @Suppress("UNCHECKED_CAST")
        val obj = load.loadOne(str) as List<String>
        obj.size shouldBe 2
    }
})
