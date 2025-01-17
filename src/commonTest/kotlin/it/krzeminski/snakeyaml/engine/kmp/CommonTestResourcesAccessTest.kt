package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer

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

    test("CommonTestResourcesFileSystem - source(...) if file exists and can be read") {
        val source = CommonTestResourcesFileSystem.source("test/resource/foo.txt".toPath())
        source.buffer().readUtf8().lines().joinToString(separator = "\n") shouldBe """
            Hello from multiplatform resources!
            Foo bar baz

        """.trimIndent()
    }

    test("CommonTestResourcesFileSystem - source(...) if file does not exist") {
        shouldThrow<IOException> {
            CommonTestResourcesFileSystem.source("test/resource/does-not-exist.txt".toPath())
        }.also {
            it.message shouldBe "Cannot read test/resource/does-not-exist.txt"
        }
    }

    test("CommonTestResourcesFileSystem - metadataOrNull(...) if file exists") {
        val metadata = CommonTestResourcesFileSystem.metadataOrNull("test/resource/foo.txt".toPath())

        metadata.shouldNotBeNull()
        metadata.isRegularFile shouldBe true
        metadata.isDirectory shouldBe false
    }

    test("CommonTestResourcesFileSystem - metadataOrNull(...) if directory exists") {
        val metadata = CommonTestResourcesFileSystem.metadataOrNull("test/resource".toPath())

        metadata.shouldNotBeNull()
        metadata.isDirectory shouldBe true
        metadata.isRegularFile shouldBe false
    }

    test("CommonTestResourcesFileSystem - metadataOrNull(...) if file does not exist") {
        val metadata = CommonTestResourcesFileSystem.metadataOrNull("test/resource/does-not-exist.txt".toPath())

        metadata.shouldBeNull()
    }

    test("CommonTestResourcesFileSystem - list(...) if directory exists") {
        val list = CommonTestResourcesFileSystem.list("test/resource".toPath())

        list.map { it.name } shouldBe listOf("foo.txt")
    }

    test("CommonTestResourcesFileSystem - list(...) if directory does not exist") {
        shouldThrow<IOException> {
            CommonTestResourcesFileSystem.list("test/does-not-exist".toPath())
        }.also {
            it.message shouldBe "Cannot list test/does-not-exist"
        }
    }

    test("CommonTestResourcesFileSystem - listOrNull(...) if directory exists") {
        val list = CommonTestResourcesFileSystem.listOrNull("test/resource".toPath())

        list.shouldNotBeNull()
        list.map { it.name } shouldBe listOf("foo.txt")
    }

    test("CommonTestResourcesFileSystem - listOrNull(...) if directory does not exist") {
        val list = CommonTestResourcesFileSystem.listOrNull("test/does-not-exist".toPath())

        list.shouldBeNull()
    }
})
