package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.JsonSchema

class NumberJsonTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(JsonSchema()).build())

    test("test all integers which are define in the core schema & JSON") {
        loader.loadOne("1") shouldBe 1
        loader.loadOne("-1") shouldBe -1
        loader.loadOne("0") shouldBe 0
        loader.loadOne("123456") shouldBe 123456
        loader.loadOne("! 1") shouldBe "1"
    }

    test("test all floats which are define in the core schema & JSON") {
        loader.loadOne("1.0") shouldBe 1.0
        loader.loadOne("-1.0") shouldBe -1.0
        loader.loadOne("0.0") shouldBe 0.0
        loader.loadOne("123.456") shouldBe 123.456
        loader.loadOne("! 1.0") shouldBe "1.0"
    }

    test("dump integer") {
        val dumper = Dump(DumpSettings.builder().setSchema(JsonSchema()).build())
        dumper.dumpToString(1) shouldBe "1\n"
        dumper.dumpToString(-1) shouldBe "-1\n"
        dumper.dumpToString(0) shouldBe "0\n"
        dumper.dumpToString(123456) shouldBe "123456\n"
    }

    test("dump float") {
        val dumper = Dump(DumpSettings.builder().setSchema(JsonSchema()).build())
        dumper.dumpToString(1.0) shouldBe "1.0\n"
        dumper.dumpToString(-1.0) shouldBe "-1.0\n"
        dumper.dumpToString(0.0) shouldBe "0.0\n"
        dumper.dumpToString(123.456) shouldBe "123.456\n"
    }
})
