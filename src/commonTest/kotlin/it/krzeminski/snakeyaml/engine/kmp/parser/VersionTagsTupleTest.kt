package it.krzeminski.snakeyaml.engine.kmp.parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VersionTagsTupleTest : FunSpec({
    test("toString") {
        val tuple = VersionTagsTuple(specVersion = null, tags = emptyMap())
        tuple.toString() shouldBe "VersionTagsTuple<null, {}>"
    }
})
