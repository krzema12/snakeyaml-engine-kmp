package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CommonTestResourcesAccessTest : FunSpec({
    test("stringFromResources - resource exists") {
        stringFromResources("test/resource/foo.txt") shouldBe """
            Hello from multiplatform resources!
            Foo bar baz

        """.trimIndent()
    }

    test("stringFromResources - file doesn't exist") {
        shouldThrow<IllegalArgumentException> {
            stringFromResources("test/resource/does-not-exist.txt")
        }.also {
            it.message shouldBe "File 'does-not-exist.txt' doesn't exist in directory '/resources/test/resource/'!"
        }
    }

    test("stringFromResources - directory doesn't exist") {
        shouldThrow<IllegalArgumentException> {
            stringFromResources("this/resource/for/sure/does/not/exist.txt")
        }.also {
            it.message shouldBe "Directory 'this' doesn't exist in directory '/resources/'!"
        }
    }
})
