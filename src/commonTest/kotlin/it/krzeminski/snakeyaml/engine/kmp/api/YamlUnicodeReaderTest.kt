package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okio.Buffer

class YamlUnicodeReaderTest : FunSpec({
    test("Detect UTF-8 dy default") {
        val reader = YamlUnicodeReader(Buffer().write(
            "1".encodeToByteArray()))

        withClue("no BOM must be detected as UTF-8") {
            reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_8
        }
        reader.readString() shouldBe "1"
    }

    test("Detect UTF-8 - EF BB BF") {
        val reader = YamlUnicodeReader(Buffer().write(byteArrayOf(
            0xEF.toByte(),
            0xBB.toByte(),
            0xBF.toByte(),
            49,
        )))

        withClue("no BOM must be detected as UTF-8") {
            reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_8
        }
        withClue("BOM must be skipped, #49 -> 1") {
            reader.readString() shouldBe "1"
        }
    }

    test("Detect 00 00 FE FF, UTF-32, big-endian") {
        val reader = YamlUnicodeReader(Buffer().write(byteArrayOf(
            0x00.toByte(),
            0x00.toByte(),
            0xFE.toByte(),
            0xFF.toByte(),
            0,
            0,
            0,
            49,
        )))

        reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_32BE
        withClue("BOM must be skipped, #49 -> 1") {
            reader.readString() shouldBe "1"
        }
    }

    test("Detect FF FE 00 00, UTF-32, little-endian") {
        val reader = YamlUnicodeReader(Buffer().write(byteArrayOf(
            0xFF.toByte(),
            0xFE.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            49,
            0,
            0,
            0,
        )))

        reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_32LE
        withClue("BOM must be skipped, #49 -> 1") {
            reader.readString() shouldBe "1"
        }
    }

    test("Detect FE FF, UTF-16, big-endian") {
        val reader = YamlUnicodeReader(Buffer().write(byteArrayOf(
            0xFE.toByte(),
            0xFF.toByte(),
            0,
            49,
        )))

        reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_16BE
        withClue("BOM must be skipped, #49 -> 1") {
            reader.readString() shouldBe "1"
        }
    }

    test("Detect FF FE, UTF-16, little-endian") {
        val reader = YamlUnicodeReader(Buffer().write(byteArrayOf(
            0xFF.toByte(),
            0xFE.toByte(),
            49,
            0,
        )))

        reader.encoding shouldBe YamlUnicodeReader.CharEncoding.UTF_16LE
        withClue("BOM must be skipped, #49 -> 1") {
            reader.readString() shouldBe "1"
        }
    }
})
