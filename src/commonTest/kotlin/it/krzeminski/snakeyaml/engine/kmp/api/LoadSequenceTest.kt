package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class LoadSequenceTest : FunSpec({
    test("Empty list [] is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val list = load.loadOne("[]") as List<Int>
        list.shouldBeEmpty()
    }

    test("list [2] is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val list = load.loadOne("[2]") as List<Int>
        list shouldBe listOf(2)
    }

    test("list [2,3] is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val list = load.loadOne("[2,3]") as List<Int>
        list shouldBe listOf(2, 3)
    }

    test("list [2,a,true] is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val list = load.loadOne("[2,a,true]") as List<Any>
        list shouldBe listOf(2, "a", true)
    }

    test("list is parsed") {
        val load = Load()
        @Suppress("UNCHECKED_CAST")
        val list = load.loadOne(stringFromResources("/load/list1.yaml")) as List<Any>
        list shouldBe listOf("a", "bb", "ccc", "dddd")
    }
})
