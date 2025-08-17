package it.krzeminski.snakeyaml.engine.kmp.schema

import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
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

    }

    test("all strings which WERE integers or doubles in YAML 1.1") {

    }

    test("all doubles which are defined in the core schema & JSON") {

    }

    test("parse special doubles which are defined in the core schema") {

    }

    test("dump special doubles which are defined in the core schema") {

    }
})
