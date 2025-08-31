package it.krzeminski.snakeyaml.engine.kmp.issues.issue17

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load

class WindowsLinesTest : FunSpec({
    test("parse Windows new line") {
        val loader = Load()
        val source = "parent:\r\n  key: value"
        val list = loader.loadOne(source) as Map<String, Any>
        list.size shouldBe 1
        list["parent"] shouldNotBe null
    }
})
