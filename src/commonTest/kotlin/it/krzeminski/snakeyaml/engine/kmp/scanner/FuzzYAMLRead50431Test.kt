package it.krzeminski.snakeyaml.engine.kmp.scanner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException

class FuzzYAMLRead50431Test : FunSpec({

    val load = Load()

    test("incomplete value should throw ScannerException") {
        shouldThrow<ScannerException> {
            load.loadOne("\"\\UE30EEE")
        }.also {
            // There's a difference in the error message between JVM and other platforms.
            // TODO: understand and fix in https://github.com/krzema12/snakeyaml-engine-kmp/issues/550
            it.message shouldContain if (platform == Platform.JVM) {
                "found unknown escape character E30EEE"
            } else {
                "found unexpected end of stream"
            }
        }
    }

    test("proper value should be parsed correctly") {
        val parsed = load.loadOne("\"\\U0000003B\"") as String
        parsed.length shouldBe 1
        parsed shouldBe "\u003B"
    }

    test("not quoted value should be parsed as literal string") {
        val parsed = load.loadOne("\\UE30EEE") as String
        parsed.length shouldBe 8
        parsed shouldBe "\\UE30EEE"
    }
})
