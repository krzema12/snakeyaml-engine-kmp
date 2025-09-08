package it.krzeminski.snakeyaml.engine.kmp.parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VersionTagsTupleTest : FunSpec({
    test("testToString") {
        val tuple = VersionTagsTuple(null, emptyMap())
        tuple.toString() shouldBe "VersionTagsTuple<null, {}>"
    }
})
