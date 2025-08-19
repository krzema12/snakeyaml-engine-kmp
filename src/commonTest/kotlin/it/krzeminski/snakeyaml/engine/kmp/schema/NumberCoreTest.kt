package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.Platform
import io.kotest.core.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class NumberCoreTest : FunSpec({
    val loader = Load(LoadSettings.builder().setSchema(CoreSchema()).build())

    test("all integers which are defined in the core schema & JSON") {
        forAll(
            table(
                headers("string", "value"),
                row("1", 1),
                row("0", 0),
                row("-0", 0),
                row("0001", 1),
                row("1234567890", 1234567890),
                row("12345678901", 12345678901L),
                // TODO implement BigInteger for platforms other than the JVM in https://github.com/krzema12/snakeyaml-engine-kmp/issues/49
                //   For the JVM, the test is in NumberCoreJvmTest.
                // row("1234567890123456789123", BigInteger("1234567890123456789123")),
            )
        ) { string: String, value: Any ->
            loader.loadOne(string) shouldBe value
        }
    }

    test("all integers which are defined in the core schema but not in JSON") {
        forAll(
            table(
                headers("string", "value"),
                row("012", 12),
                row("0xFF", 255),
                row("0o123", 83),
                row("0o128", "0o128"),
                // start with +
                row("+1", 1),
                row("+1223344", 1223344),
                row("+12.23344", 12.23344),
                row("+0.23344", 0.23344),
                row("+0", 0),
                // leading zero
                row("03", 3),
                row("03.67", 3.67),
            )
        ) { string: String, value: Any ->
            loader.loadOne(string) shouldBe value
        }
    }

    test("all strings which WERE integers or doubles in YAML 1.1") {
        forAll(
            table(
                headers("string", "value"),
                row("12:10:02", "12:10:02"),
                row("0b1010", "0b1010"),
                row("1_000", "1_000"),
                row("1_000.5", "1_000.5"),
                row("-0xFF", "-0xFF"),
                row("+0xFF", "+0xFF"),
                row("+0o123", "+0o123"),
                row("-0o123", "-0o123"),
                row("! 3.6", "3.6"),
                row("! 3", "3"),
            )
        ) { string: String, value: Any ->
            loader.loadOne(string) shouldBe value
        }
    }

    test("all doubles which are defined in the core schema & JSON") {
        forAll(
            table(
                headers("string", "value"),
                row("-1.345", -1.345),
                row("0.0", 0.0),
                row("-0.0", 0.0),
                row("0.123", 0.123),
                row("1.23e-6", 1.23E-6),
                row("1.23e+6", 1.23E6),
                row("1.23e6", 1.23E6),
                row("1.23E6", 1.23E6),
                row("-1.23e6", -1.23E6),
                row("1000.25", 1000.25),
                row("9000.0", 9000.0),
                row("1.", 1.0),
            )
        ) { string: String, value: Any ->
            loader.loadOne(string) shouldBe value
        }
    }

    test("parse special doubles which are defined in the core schema") {
        forAll(
            table(
                headers("string", "value"),
                row(".inf", Double.POSITIVE_INFINITY),
                row(".Inf", Double.POSITIVE_INFINITY),
                row(".INF", Double.POSITIVE_INFINITY),
                row("-.inf", Double.NEGATIVE_INFINITY),
                row("-.Inf", Double.NEGATIVE_INFINITY),
                row("-.INF", Double.NEGATIVE_INFINITY),
                row(".nan", Double.NaN),
                row(".NaN", Double.NaN),
                row(".NAN", Double.NaN),
            )
        ) { string: String, value: Any ->
            loader.loadOne(string)!! shouldBeEqual value
        }
    }

    test("dump special doubles which are defined in the core schema") {
        val dumper = Dump(DumpSettings.builder().setSchema(CoreSchema()).build())
        forAll(
            table(
                headers("value", "string"),
                row(Double.POSITIVE_INFINITY, if (platform != Platform.JS) ".inf\n" else "!!int 'Infinity'\n"),
                row(Float.POSITIVE_INFINITY, if (platform != Platform.JS) ".inf\n" else "!!int 'Infinity'\n"),
                row(Double.NEGATIVE_INFINITY, if (platform != Platform.JS) "-.inf\n" else "!!int '-Infinity'\n"),
                row(Float.NEGATIVE_INFINITY, if (platform != Platform.JS) "-.inf\n" else "!!int '-Infinity'\n"),
                row(Double.NaN, if (platform != Platform.JS) ".nan\n" else "!!int 'NaN'\n"),
                row(Float.NaN, if (platform != Platform.JS) ".nan\n" else "!!int 'NaN'\n"),
            )
        ) { value: Any, string: String ->
            dumper.dumpToString(value) shouldBe string
        }
    }
})
