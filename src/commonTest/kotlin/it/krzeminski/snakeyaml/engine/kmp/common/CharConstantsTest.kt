package it.krzeminski.snakeyaml.engine.kmp.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.common.CharConstants.Companion.ESCAPE_REPLACEMENTS

class CharConstantsTest : FunSpec({

    test("LINEBR contains only LF and CR: http://www.yaml.org/spec/1.2/spec.html#id2774608") {
        CharConstants.LINEBR.has('\n'.code) shouldBe true // "LF must be included"
        CharConstants.LINEBR.has('\r'.code) shouldBe false // "CR must be excluded"
        CharConstants.LINEBR.hasNo('\u0085'.code) shouldBe true // "85 (next line) must not be included in 1.2"
        CharConstants.LINEBR.hasNo('\u2028'.code) shouldBe true // "2028 (line separator) must not be included in 1.2"
        CharConstants.LINEBR.hasNo('\u2029'.code) shouldBe true // "2029 (paragraph separator) must not be included in 1.2"
        CharConstants.LINEBR.hasNo('a'.code) shouldBe true // "normal char should not be included"
    }

    test("NULL_OR_LINEBR contains 3 chars") {
        CharConstants.NULL_OR_LINEBR.has('\n'.code) shouldBe true
        CharConstants.NULL_OR_LINEBR.has('\r'.code) shouldBe true
        CharConstants.NULL_OR_LINEBR.has('\u0000'.code) shouldBe true
        CharConstants.NULL_OR_LINEBR.has('\u0085'.code) shouldBe false // "85 (next line) must not be included in 1.2"
        CharConstants.NULL_OR_LINEBR.has('\u2028'.code) shouldBe false // "2028 (line separator) must not be included in 1.2"
        CharConstants.NULL_OR_LINEBR.has('\u2029'.code) shouldBe false // "2029 (paragraph separator) must not be included in 1.2"
        CharConstants.NULL_OR_LINEBR.has('b'.code) shouldBe false // "normal char should not be included"
    }

    test("additional chars") {
        CharConstants.NULL_BL_LINEBR.hasNo('1'.code) shouldBe true
        CharConstants.NULL_BL_LINEBR.has('1'.code, "123") shouldBe true
        CharConstants.NULL_BL_LINEBR.hasNo('4'.code, "123") shouldBe true
    }

    test("ESCAPE_REPLACEMENTS") {
        'a'.code shouldBe 97
        ESCAPE_REPLACEMENTS.size shouldBe 15
        ESCAPE_REPLACEMENTS['r'] shouldBe "\r"
    }

    test("escapeChar") {
        CharConstants.escapeChar(' ') shouldBe " "
        CharConstants.escapeChar('/') shouldBe "/"
        CharConstants.escapeChar('\t') shouldBe "\\t"
    }
})
