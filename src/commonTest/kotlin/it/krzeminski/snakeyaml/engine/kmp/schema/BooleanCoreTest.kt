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

class BooleanCoreTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())

    test("parse boolean") {
        // true | True | TRUE | false | False | FALSE
        forAll(
            table(
                headers("input", "expected"),
                row("true", true),
                row("True", true),
                row("TRUE", true),
                row("false", false),
                row("False", false),
                row("FALSE", false),
                row("! true", "true")
            )
        ) { input, expected ->
            loader.loadOne(input) shouldBe expected
        }
    }

    test("dump boolean") {
        val dumper = Dump(DumpSettings.builder().setSchema(CoreSchema()).build())
        forAll(
            table(
                headers("input", "expected"),
                row(true, "true\n"),
                row(false, "false\n")
            )
        ) { input, expected ->
            dumper.dumpToString(input) shouldBe expected
        }
    }
})
