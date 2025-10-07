package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.common.Platform
import io.kotest.common.platform
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

class NumberJsonTest : FunSpec({
    val loader = Load(LoadSettings(schema = JsonSchema()))

    test("test all integers which are define in the core schema & JSON") {
        forAll(
            table(
                headers("input", "expected"),
                row("1", 1),
                row("-1", -1),
                row("0", 0),
                row("123456", 123456),
                row("! 1", "1")
            )
        ) { input, expected ->
            loader.loadOne(input) shouldBe expected
        }
    }

    test("test all floats which are define in the core schema & JSON") {
        forAll(
            table(
                headers("input", "expected"),
                row("1.0", 1.0),
                row("-1.0", -1.0),
                row("0.0", 0.0),
                row("123.456", 123.456),
                row("! 1.0", "1.0")
            )
        ) { input, expected ->
            loader.loadOne(input) shouldBe expected
        }
    }

    test("dump integer") {
        val dumper = Dump(DumpSettings(schema = JsonSchema()))
        forAll(
            table(
                headers("input", "expected"),
                row(1, "1\n"),
                row(-1, "-1\n"),
                row(0, "0\n"),
                row(123456, "123456\n")
            )
        ) { input, expected ->
            dumper.dumpToString(input) shouldBe expected
        }
    }

    test("dump float") {
        val dumper = Dump(DumpSettings(schema = JsonSchema()))
        forAll(
            table(
                headers("input", "expected"),
                // FIXME: There's a different behavior for JS, and it's a bug.
                //  Tracking in https://github.com/krzema12/snakeyaml-engine-kmp/issues/526.
                row(1.0, if (platform != Platform.JS) "1.0\n" else "1\n"),
                row(-1.0, if (platform != Platform.JS) "-1.0\n" else "-1\n"),
                row(0.0, if (platform != Platform.JS) "0.0\n" else "0\n"),
                row(123.456, if (platform != Platform.JS) "123.456\n" else "!!int '123.456'\n")
            )
        ) { input, expected ->
            dumper.dumpToString(input) shouldBe expected
        }
    }
})
