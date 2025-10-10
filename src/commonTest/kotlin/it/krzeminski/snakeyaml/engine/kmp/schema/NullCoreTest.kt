package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.matchers.shouldBe
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class NullCoreTest : FunSpec({
    val loader = Load(LoadSettings(schema = CoreSchema()))

    test("parse null") {
        forAll(
            table(
                headers("input", "expected"),
                row("null", null),
                row("Null", null),
                row("NULL", null),
                row("~", null),
                row("! null", "null")
            )
        ) { input, expected ->
            loader.loadOne(input) shouldBe expected
        }
    }

    test("dump null") {
        val dumper = Dump(DumpSettings(schema = CoreSchema()))
        dumper.dumpToString(null) shouldBe "null\n"
    }
})
