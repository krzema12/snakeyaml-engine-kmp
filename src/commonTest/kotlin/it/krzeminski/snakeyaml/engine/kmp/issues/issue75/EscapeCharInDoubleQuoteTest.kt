@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.issues.issue75

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException

class EscapeCharInDoubleQuoteTest : FunSpec({
    val load = Load(LoadSettings())

    test("DEL allowed in double-quoted scalar") {
        val str = "\"\u007F\""
        val parsed = load.loadOne(str) as String
        parsed shouldBe "\u007F"
    }

    test("DEL allowed in single-quoted scalar") {
        val str = "'\u007F'"
        val parsed = load.loadOne(str) as String
        parsed shouldBe "\u007F"
    }

    test("DEL rejected in plain scalar value") {
        val str = "key: \u007F"
        shouldThrow<ScannerException> {
            load.loadOne(str)
        }.also {
            it.message shouldContain "DEL character (0x7F) is not allowed in plain scalars"
        }
    }

    test("DEL rejected in plain scalar key") {
        val str = "ke\u007Fy: value"
        shouldThrow<ScannerException> {
            load.loadOne(str)
        }.also {
            it.message shouldContain "DEL character (0x7F) is not allowed in plain scalars"
        }
    }

    test("DEL rejected in comment") {
        val settings = LoadSettings(parseComments = true)
        val loadWithComments = Load(settings)
        val str = "key: value # comment with \u007F"
        shouldThrow<ScannerException> {
            loadWithComments.loadOne(str)
        }.also {
            it.message shouldContain "DEL character (0x7F) is not allowed in comments"
        }
    }

    test("DEL rejected in literal block scalar") {
        val str = "|\n  text with \u007F"
        shouldThrow<ScannerException> {
            load.loadOne(str)
        }.also {
            it.message shouldContain "DEL character (0x7F) is not allowed in block scalars"
        }
    }

    test("DEL rejected in folded block scalar") {
        val str = ">\n  text with \u007F"
        shouldThrow<ScannerException> {
            load.loadOne(str)
        }.also {
            it.message shouldContain "DEL character (0x7F) is not allowed in block scalars"
        }
    }
})
