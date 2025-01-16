package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okio.IOException

class CommonTestResourcesAccessTest : FunSpec({
    test("stringFromResources - resource exists") {
        stringFromResources("/test/resource/foo.txt") shouldBe """
            Hello from multiplatform resources!
            Foo bar baz

        """.trimIndent()
    }

    test("stringFromResources - file doesn't exist") {
        shouldThrow<IOException> {
            stringFromResources("/test/resource/does-not-exist.txt")
        }.also {
            it.message shouldBe "Cannot read test/resource/does-not-exist.txt"
        }
    }

    test("stringFromResources - directory doesn't exist") {
        shouldThrow<IOException> {
            stringFromResources("/this/resource/for/sure/does/not/exist.txt")
        }.also {
            it.message shouldBe "Cannot read this/resource/for/sure/does/not/exist.txt"
        }
    }

    test("stringFromResources - no leading slash") {
        shouldThrow<IllegalArgumentException> {
            stringFromResources("test/resource/foo.txt")
        }.also {
            it.message shouldBe "A leading slash is required!"
        }
    }
})
