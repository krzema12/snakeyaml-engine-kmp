package it.krzeminski.snakeyaml.engine.kmp.usecases.json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle

/**
 * https://bitbucket.org/snakeyaml/snakeyaml/issues/1084/dump-as-json
 */
class DumperJsonTest : FunSpec({
    val dumper =
        Dump(DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.FLOW)
            .setDefaultScalarStyle(ScalarStyle.JSON_SCALAR_STYLE)
            .build())

    test("JSON str") {
        dumper.dumpToString("bar1") shouldBe "\"bar1\"\n"
    }

    test("JSON int") {
        dumper.dumpToString(17) shouldBe "17\n"
    }

    test("JSON int as str") {
        dumper.dumpToString("17") shouldBe "\"17\"\n"
    }

    test("JSON int in collection") {
        val list = listOf(17, "17")
        dumper.dumpToString(list) shouldBe "[17, \"17\"]\n"
    }

    test("JSON boolean") {
        dumper.dumpToString(true) shouldBe "true\n"
    }

    test("JSON boolean in collection") {
        val list = listOf(true, "true")
        dumper.dumpToString(list) shouldBe "[true, \"true\"]\n"
    }

    test("JSON null") {
        dumper.dumpToString(null) shouldBe "null\n"
    }

    test("JSON null in collection") {
        val list = listOf(null, "null")
        dumper.dumpToString(list) shouldBe "[null, \"null\"]\n"
    }

    test("JSON") {
        val list = listOf(
            17,
            "foo",
            true,
            "true",
            false,
            "false",
            null,
            "null",
        )
        dumper.dumpToString(list) shouldBe "[17, \"foo\", true, \"true\", false, \"false\", null, \"null\"]\n"
    }

    test("JSON object") {
        val map = mapOf(
            "str1" to "foo",
            "bool" to true,
            "strBool" to "true",
            "null" to null,
            "strNull" to "null",
        )
        dumper.dumpToString(map) shouldBe "{\"str1\": \"foo\", \"bool\": true, \"strBool\": \"true\", \"null\": null, \"strNull\": \"null\"}\n"
    }

    test("JSON binary") {
        val map = mapOf(
            "binary" to byteArrayOf(8, 14, 15, 10, 126, 32, 65, 65, 65),
        )
        dumper.dumpToString(map) shouldBe "{\"binary\": !!binary \"CA4PCn4gQUFB\"}\n"
    }
})
