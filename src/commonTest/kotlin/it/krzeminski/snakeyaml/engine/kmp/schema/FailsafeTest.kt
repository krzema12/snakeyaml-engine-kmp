package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.schema.FailsafeSchema

class FailsafeTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(FailsafeSchema()).build())

    test("parse string") {
        loader.loadOne("true") shouldBe "true"
        loader.loadOne("false") shouldBe "false"
        loader.loadOne("null") shouldBe "null"
        loader.loadOne("1") shouldBe "1"
        loader.loadOne("0001") shouldBe "0001"
        loader.loadOne("3.000") shouldBe "3.000"
    }

    test("dump string") {
        val dumper = Dump(DumpSettings.builder().setSchema(FailsafeSchema()).build())
        dumper.dumpToString(true) shouldBe "!!bool 'true'\n"
        dumper.dumpToString(false) shouldBe "!!bool 'false'\n"
        dumper.dumpToString(null) shouldBe "!!null 'null'\n"
        dumper.dumpToString(25) shouldBe "!!int '25'\n"
        dumper.dumpToString(17) shouldBe "!!int '17'\n"
        dumper.dumpToString(17.4) shouldBe "!!float '17.4'\n"
    }
})
