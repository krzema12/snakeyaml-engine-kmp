package it.krzeminski.snakeyaml.engine.kmp.issues.issue17

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

/**
 * https://yaml.org/spec/1.2/spec.html#id2774608
 */
class WindowsTest : FunSpec({
    val loader = Load()

    test("check that Windows style line endings handled the same as Unix style ones") {
        val settings = LoadSettings()
        val reader1 = StreamReader(settings, "foo\r\nbar")
        val reader2 = StreamReader(settings, "foo\nbar")
        reader1.forward(100)
        reader2.forward(100)
        reader1.line shouldBe reader2.line
    }

    test("count lines CRLF") {
        shouldThrow<ParserException> {
            loader.loadOne("\r\n[")
        }.also {
            it.message shouldContain "line 2,"
        }
    }

    test("count lines CRCR") {
        shouldThrow<ParserException> {
            loader.loadOne("\r\r[")
        }.also {
            it.message shouldContain "line 3,"
        }
    }

    test("count lines LFLF") {
        shouldThrow<ParserException> {
            loader.loadOne("\n\n[")
        }.also {
            it.message shouldContain "line 3,"
        }
    }
})
