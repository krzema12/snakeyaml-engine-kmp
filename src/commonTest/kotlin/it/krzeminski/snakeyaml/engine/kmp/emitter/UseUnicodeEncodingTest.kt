package it.krzeminski.snakeyaml.engine.kmp.emitter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings

class UseUnicodeEncodingTest : FunSpec({

    test("emit unicode") {
        val settings = DumpSettings.builder().build()
        val dump = Dump(settings)
        val russianUnicode = "Пушкин - это наше всё! 😊"
        dump.dumpToString(russianUnicode) shouldBe "$russianUnicode\n"
    }

    test("escape unicode") {
        val settings = DumpSettings.builder().setUseUnicodeEncoding(false).build()
        val dump = Dump(settings)
        val russianUnicode = "Пушкин - это наше всё! 😊"
        dump.dumpToString(russianUnicode) shouldBe "\"\\u041f\\u0443\\u0448\\u043a\\u0438\\u043d - \\u044d\\u0442\\u043e \\u043d\\u0430\\u0448\\u0435\\\n" +
            "  \\ \\u0432\\u0441\\u0451! \\U0001f60a\"\n"
    }
})
