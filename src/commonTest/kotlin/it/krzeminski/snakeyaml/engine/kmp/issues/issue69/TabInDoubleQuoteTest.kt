@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.issues.issue69

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class TabInDoubleQuoteTest : FunSpec({
    test("tab in double quote") {
        val options = LoadSettings.builder().setParseComments(true).build()
        val load = Load(options)
        val str = "- \"\\\t\"" // "\TAB"
        val obj = load.loadOne(str) as List<String>
        obj shouldHaveSize 1
        obj[0] shouldBe "\t"
    }
})
