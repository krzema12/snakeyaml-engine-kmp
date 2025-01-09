package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class LoadMappingTest: FunSpec({
    test("Empty map {} is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val map = load.loadOne("{}") as Map<Int, Int>
        map shouldBe LoadSettings.builder().build().defaultMap.invoke(0)
    }

    test("map {a: 1} is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val map = load.loadOne("{a: 1}") as Map<String, Int>
        map shouldBe mapOf("a" to 1)
    }

    test("map {a: 1, b: 2} is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val map = load.loadOne("a: 1\nb: 2\nc:\n  - aaa\n  - bbb") as Map<String, Any>
        map shouldBe mapOf("a" to 1, "b" to 2, "c" to listOf("aaa", "bbb"))
    }

    test("map {x: 1, y: 2, z:3} is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val map = load.loadOne(stringFromResources("load/map1.yaml")) as Map<String, Any>
        map shouldBe mapOf("x" to 1, "y" to 2, "z" to 3)
    }
})
