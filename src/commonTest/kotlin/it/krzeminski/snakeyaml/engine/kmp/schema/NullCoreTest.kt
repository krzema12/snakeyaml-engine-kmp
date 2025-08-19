package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class NullCoreTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())

    test("parse null") {
        // null | Null | NULL | ~
        loader.loadOne("null") shouldBe null
        loader.loadOne("Null") shouldBe null
        loader.loadOne("NULL") shouldBe null
        loader.loadOne("~") shouldBe null
        loader.loadOne("! null") shouldBe "null"
    }

    test("dump null") {
        val dumper = Dump(DumpSettings.builder().setSchema(CoreSchema()).build())
        dumper.dumpToString(null) shouldBe "null\n"
    }
})
