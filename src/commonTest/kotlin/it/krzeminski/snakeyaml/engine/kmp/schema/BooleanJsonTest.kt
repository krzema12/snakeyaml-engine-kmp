package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema

class BooleanJsonTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(JsonSchema()).build())

    test("parse boolean") {
        loader.loadOne("true") shouldBe true
        loader.loadOne("false") shouldBe false
        loader.loadOne("! true") shouldBe "true"
    }

    test("dump boolean") {
        val dumper = Dump(DumpSettings.builder().setSchema(JsonSchema()).build())
        dumper.dumpToString(true) shouldBe "true\n"
        dumper.dumpToString(false) shouldBe "false\n"
    }
})
