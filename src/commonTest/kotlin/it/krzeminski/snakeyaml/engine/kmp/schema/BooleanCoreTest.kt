package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.CoreSchema

class BooleanCoreTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())

    test("parse boolean") {
        // true | True | TRUE | false | False | FALSE
        loader.loadOne("true") shouldBe true
        loader.loadOne("True") shouldBe true
        loader.loadOne("TRUE") shouldBe true
        loader.loadOne("false") shouldBe false
        loader.loadOne("False") shouldBe false
        loader.loadOne("FALSE") shouldBe false
        loader.loadOne("! true") shouldBe "true"
    }

    test("dump boolean") {
        val dumper = Dump(DumpSettings.builder().setSchema(CoreSchema()).build())
        dumper.dumpToString(true) shouldBe "true\n"
        dumper.dumpToString(false) shouldBe "false\n"
    }
})
