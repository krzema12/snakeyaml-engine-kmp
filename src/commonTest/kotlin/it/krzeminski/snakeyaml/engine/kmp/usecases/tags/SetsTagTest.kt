@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.usecases.tags

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

/**
 * Example of parsing a local tag
 */
class SetsTagTest : FunSpec({
    test("!!set tag creates a Set") {
        val loader = Load()
        val yaml =
            """|---
               |sets: !!set
               |    ? a
               |    ? b
            """.trimMargin()
        val map = loader.loadOne(yaml) as Map<String, Set<String>>
        val set = map["sets"]!!
        set.size shouldBe 2
        val iter = set.iterator()
        iter.next() shouldBe "a"
        iter.next() shouldBe "b"
    }
})
