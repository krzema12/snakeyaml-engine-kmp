package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema

class NullJsonTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(JsonSchema()).build())

    test("parse null") {
        loader.loadOne("null") shouldBe null
        loader.loadOne("! null") shouldBe "null"
    }

    test("dump null") {
        val dumper = Dump(DumpSettings.builder().setSchema(JsonSchema()).build())
        dumper.dumpToString(null) shouldBe "null\n"
    }
})
