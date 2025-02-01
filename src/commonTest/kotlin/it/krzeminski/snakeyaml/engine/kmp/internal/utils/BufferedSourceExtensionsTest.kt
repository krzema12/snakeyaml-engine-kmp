package it.krzeminski.snakeyaml.engine.kmp.internal.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okio.Buffer

class BufferedSourceExtensionsTest : FunSpec(
    {
        test("reads empty string from empty buffer") {
            Buffer().readUtf8WithLimit(1024) shouldBe ""
        }

        test("reads ascii string from buffer") {
            Buffer().writeUtf8("abc")
                .readUtf8WithLimit(1024) shouldBe "abc"
        }

        test("reads 2 bytes unicode string from buffer") {
            Buffer().writeUtf8("абв")
                .readUtf8WithLimit(1024) shouldBe "абв"
        }

        test("reads 3 bytes unicode string from buffer") {
            Buffer().writeUtf8("੨੩੪")
                .readUtf8WithLimit(1024) shouldBe "੨੩੪"
        }

        test("reads 4 bytes unicode string from buffer") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            Buffer().writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
                .readUtf8WithLimit(1024) shouldBe "\uD800\uDF00\uD800\uDF01\uD800\uDF02"
        }

        test("reads ascii string from buffer up to the limit") {
            Buffer().writeUtf8("abcdefghijklmnop")
                .readUtf8WithLimit(3) shouldBe "abc"
        }

        test("reads 2 bytes unicode string from buffer up to the limit") {
            // each cyrillic letter is 2 bytes long
            // 5 bytes is a half of the 3rd letter
            Buffer().writeUtf8("абвгдежзиклмн")
                .readUtf8WithLimit(5) shouldBe "аб"
        }

        test("reads 2 bytes unicode string from buffer up to the limit less than size of one codepoint") {
            Buffer().writeUtf8("абвгдежзиклмн")
                .readUtf8WithLimit(1) shouldBe ""
        }

        test("reads 3 bytes unicode string from buffer up to the limit") {
            Buffer().writeUtf8("੨੩੪੨੩੪")
                .readUtf8WithLimit(4) shouldBe "੨"
        }

        test("reads 3 bytes unicode string from buffer up to the limit less than size of one codepoint") {
            val buffer = Buffer().writeUtf8("੨੩੪੨੩੪")
            buffer.readUtf8WithLimit(2) shouldBe ""
            buffer.readUtf8WithLimit(1) shouldBe ""
        }

        test("reads 4 bytes unicode string from buffer up to the limit") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            Buffer().writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
                .readUtf8WithLimit(5) shouldBe "\uD800\uDF00"
        }

        test("reads 4 bytes unicode string from buffer up to the limit less than size of one codepoint") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            val buffer = Buffer().writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
            buffer.readUtf8WithLimit(3) shouldBe ""
            buffer.readUtf8WithLimit(2) shouldBe ""
            buffer.readUtf8WithLimit(1) shouldBe ""
        }

        test("reads ascii string from buffer up to the limit continuously") {
            val buffer = Buffer().writeUtf8("abcdefgh")
            buffer.readUtf8WithLimit(3) shouldBe "abc"
            buffer.readUtf8WithLimit(3) shouldBe "def"
            buffer.readUtf8WithLimit(3) shouldBe "gh"
            buffer.readUtf8WithLimit(3) shouldBe ""
        }

        test("reads 2 bytes unicode string from buffer up to the limit continuously") {
            // each letter is 2 bytes long
            // 5 bytes is a half of the 3rd letter
            val buffer = Buffer().writeUtf8("абвгд")
            buffer.readUtf8WithLimit(5) shouldBe "аб"
            buffer.readUtf8WithLimit(5) shouldBe "вг"
            buffer.readUtf8WithLimit(5) shouldBe "д"
            buffer.readUtf8WithLimit(3) shouldBe ""
        }

        test("reads 3 bytes unicode string from buffer up to the limit continuously") {
            val buffer = Buffer().writeUtf8("੨੩੪")
            buffer.readUtf8WithLimit(4) shouldBe "੨"
            buffer.readUtf8WithLimit(4) shouldBe "੩"
            buffer.readUtf8WithLimit(4) shouldBe "੪"
            buffer.readUtf8WithLimit(4) shouldBe ""
        }

        test("reads 4 bytes unicode string from buffer up to the limit continuously") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            val buffer = Buffer().writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
            buffer.readUtf8WithLimit(4) shouldBe "\uD800\uDF00"
            buffer.readUtf8WithLimit(4) shouldBe "\uD800\uDF01"
            buffer.readUtf8WithLimit(4) shouldBe "\uD800\uDF02"
            buffer.readUtf8WithLimit(4) shouldBe ""
        }

        test("reads mix of unicode and ascii from buffer") {
            Buffer().writeUtf8("abcабв")
                .readUtf8WithLimit(1024) shouldBe "abcабв"
        }

        test("reads mix of unicode and ascii from buffer up to the limit") {
            Buffer().writeUtf8("aяbюcэ")
                .readUtf8WithLimit(3) shouldBe "aя"
        }

        test("reads mix of unicode and ascii from buffer up to the limit continuously") {
            val buffer = Buffer().writeUtf8("aяbюcэ")
            buffer.readUtf8WithLimit(3) shouldBe "aя"
            buffer.readUtf8WithLimit(3) shouldBe "bю"
            buffer.readUtf8WithLimit(3) shouldBe "cэ"
            buffer.readUtf8WithLimit(3) shouldBe ""
        }

        test("reads a long unicode string from buffer with smaller step") {
            val originalString = "я".repeat(10_000)
            val buffer = Buffer().writeUtf8(originalString)
            val result = buildString {
                while (true) {
                    // the odd limit to read half of the original bytes
                    val read = buffer.readUtf8WithLimit(1021)
                    if (read.isEmpty()) break
                    append(read)
                }
            }

            result shouldBe originalString
        }

        test("ill-formed bytes in the middle does not affect remaining bytes") {
            val buffer = Buffer()
                .writeUtf8("abc")
                .writeByte(0xBB)
                .writeUtf8("def")

            buffer.readUtf8WithLimit(3) shouldBe "abc"
            buffer.readUtf8WithLimit(3) shouldBe "�de"
            buffer.readUtf8WithLimit(3) shouldBe "f"
            buffer.readUtf8WithLimit(3) shouldBe ""
        }

        test("ill-formed byte at the boundary of request does not affect remaining ascii bytes") {
            val buffer = Buffer()
                .writeUtf8("abc")
                .writeByte(0xBB)
                .writeUtf8("def")

            buffer.readUtf8WithLimit(4) shouldBe "abc�"
            buffer.readUtf8WithLimit(4) shouldBe "def"
            buffer.readUtf8WithLimit(4) shouldBe ""
        }

        test("ill-formed bytes at the boundary of request does not affect remaining ascii bytes") {
            val buffer = Buffer()
                .writeUtf8("abc")
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeUtf8("def")

            buffer.readUtf8WithLimit(4) shouldBe "abc�"
            buffer.readUtf8WithLimit(4) shouldBe "�def"
            buffer.readUtf8WithLimit(4) shouldBe ""
        }

        test("ill-formed byte at the boundary of request does not affect remaining 2 bytes unicode") {
            val buffer = Buffer()
                .writeUtf8("абв")
                .writeByte(0xBB)
                .writeUtf8("где")

            buffer.readUtf8WithLimit(7) shouldBe "абв�"
            buffer.readUtf8WithLimit(7) shouldBe "где"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("ill-formed bytes at the boundary of request does not affect remaining 2 bytes unicode") {
            val buffer = Buffer()
                .writeUtf8("абв")
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeUtf8("где")

            buffer.readUtf8WithLimit(7) shouldBe "абв�"
            buffer.readUtf8WithLimit(7) shouldBe "�где"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("ill-formed byte at the boundary of request does not affect remaining 3 bytes unicode") {
            val buffer = Buffer()
                .writeUtf8("੨੩੪")
                .writeByte(0xBB)
                .writeUtf8("੨੩੪")

            buffer.readUtf8WithLimit(10) shouldBe "੨੩੪�"
            buffer.readUtf8WithLimit(10) shouldBe "੨੩੪"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("ill-formed bytes at the boundary of request does not affect remaining 3 bytes unicode") {
            val buffer = Buffer()
                .writeUtf8("੨੩੪")
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeUtf8("੨੩੪")

            buffer.readUtf8WithLimit(10) shouldBe "੨੩੪�"
            buffer.readUtf8WithLimit(10) shouldBe "�੨੩੪"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("ill-formed byte at the boundary of request does not affect remaining 4 bytes unicode") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            val buffer = Buffer()
                .writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
                .writeByte(0xBB)
                .writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")

            buffer.readUtf8WithLimit(13) shouldBe "\uD800\uDF00\uD800\uDF01\uD800\uDF02�"
            buffer.readUtf8WithLimit(13) shouldBe "\uD800\uDF00\uD800\uDF01\uD800\uDF02"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("ill-formed bytes at the boundary of request does not affect remaining 4 bytes unicode") {
            // https://www.compart.com/en/unicode/U+10300
            // https://www.compart.com/en/unicode/U+10301
            // https://www.compart.com/en/unicode/U+10302
            val buffer = Buffer()
                .writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeUtf8("\uD800\uDF00\uD800\uDF01\uD800\uDF02")

            buffer.readUtf8WithLimit(13) shouldBe "\uD800\uDF00\uD800\uDF01\uD800\uDF02�"
            buffer.readUtf8WithLimit(13) shouldBe "�\uD800\uDF00\uD800\uDF01\uD800\uDF02"
            buffer.readUtf8WithLimit(7) shouldBe ""
        }

        test("reads all requested bytes in buffer ends before a valid codepoint found") {
            val buffer = Buffer()
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)

            buffer.readUtf8WithLimit(2) shouldBe "��"
            buffer.readUtf8WithLimit(2) shouldBe "��"
            buffer.readUtf8WithLimit(2) shouldBe ""
        }

        test("reads all requested bytes if no valid codepoint found within 4 bytes") {
            val buffer = Buffer()
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)
                .writeByte(0xBB)

            buffer.readUtf8WithLimit(5) shouldBe "�����"
            buffer.readUtf8WithLimit(2) shouldBe "�"
            buffer.readUtf8WithLimit(2) shouldBe ""
        }
    }
)
