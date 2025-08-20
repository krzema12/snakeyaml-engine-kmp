package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.Platform
import io.kotest.core.platform
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
    val loader = Load(LoadSettings.builder().setSchema(FailsafeSchema()).build())

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
        val dumper = Dump(DumpSettings.builder().setSchema(FailsafeSchema()).build())
        forAll(
            table(
                headers("input", "expected"),
                row(true, "!!bool 'true'\n"),
                row(false, "!!bool 'false'\n"),
                row(null, "!!null 'null'\n"),
                row(25, "!!int '25'\n"),
                row(17, "!!int '17'\n"),
                // FIXME: There's a different behavior for JS, and it's a bug.
                //  Tracking in https://github.com/krzema12/snakeyaml-engine-kmp/issues/526.
                row(17.4, if (platform != Platform.JS) "!!float '17.4'\n" else "!!int '17.4'\n"),
            )
        ) { input, expected ->
            dumper.dumpToString(input) shouldBe expected
        }
    }
})
