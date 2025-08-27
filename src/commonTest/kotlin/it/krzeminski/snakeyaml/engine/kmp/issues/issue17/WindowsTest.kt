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

    test("Check that Windows style line endings handled the same as Unix style ones") {
        val settings = LoadSettings.builder().build()
        val reader1 = StreamReader(settings, "foo\r\nbar")
        val reader2 = StreamReader(settings, "foo\nbar")
        reader1.forward(100)
        reader2.forward(100)
        reader1.line shouldBe reader2.line
    }

    test("Count lines CRLF") {
        val exception = shouldThrow<ParserException> {
            loader.loadOne("\r\n[")
        }
        exception.message shouldContain "line 2,"
    }

    test("Count lines CRCR") {
        val exception = shouldThrow<ParserException> {
            loader.loadOne("\r\r[")
        }
        exception.message shouldContain "line 3,"
    }

    test("Count lines LFLF") {
        val exception = shouldThrow<ParserException> {
            loader.loadOne("\n\n[")
        }
        exception.message shouldContain "line 3,"
    }
})
