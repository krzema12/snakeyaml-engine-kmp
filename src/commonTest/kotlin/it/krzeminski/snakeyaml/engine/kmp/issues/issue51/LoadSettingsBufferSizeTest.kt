package it.krzeminski.snakeyaml.engine.kmp.issues.issue51

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.composer.Composer
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ParserException
import it.krzeminski.snakeyaml.engine.kmp.parser.ParserImpl
import it.krzeminski.snakeyaml.engine.kmp.scanner.StreamReader

private fun parse(yaml: String) {
    val settings = LoadSettings.builder().setBufferSize(yaml.length).build()
    Composer(settings, ParserImpl(settings, StreamReader(settings, yaml)))
        .getSingleNode()
}

class LoadSettingsBufferSizeTest : FunSpec({
    val yaml = " - foo: bar\n" + "   if: 'aaa' == 'bbb'"
    val expectedError = "while parsing a block mapping\n" +
            " in reader, line 1, column 4:\n" + "     - foo: bar\n" + "       ^\n" +
            "expected <block end>, but found '<scalar>'\n" + " in reader, line 2, column 14:\n" +
            "       if: 'aaa' == 'bbb'\n" + "                 ^\n"

    test("Issue 51 - exact buffer size") {
        val exception = shouldThrow<ParserException> { parse(yaml) }
        exception.message shouldBe expectedError
    }
})
