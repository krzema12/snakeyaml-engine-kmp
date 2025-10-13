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

class FailsafeTest : FunSpec({
    val loader = Load(LoadSettings(schema = FailsafeSchema()))

    test("parse string") {
        forAll(
            table(
                headers("input", "expected"),
                row("true", "true"),
                row("false", "false"),
                row("null", "null"),
                row("1", "1"),
                row("0001", "0001"),
                row("3.000", "3.000")
            )
        ) { input, expected ->
            loader.loadOne(input) shouldBe expected
        }
    }

    test("dump string") {
        val dumper = Dump(DumpSettings(schema = FailsafeSchema()))
        forAll(
            table(
                headers("input", "expected"),
                row(true, "!!bool 'true'\n"),
                row(false, "!!bool 'false'\n"),
                row(null, "!!null 'null'\n"),
                row(12.toByte(), "!!int '12'\n"),
                row(45.toShort(), "!!int '45'\n"),
                row(25, "!!int '25'\n"),
                row(17, "!!int '17'\n"),
                row(34L, "!!int '34'\n"),
                row(17.4, "!!float '17.4'\n"),
                row(23.5, "!!float '23.5'\n"),
            )
        ) { input, expected ->
            dumper.dumpToString(input) shouldBe expected
        }
    }
})
