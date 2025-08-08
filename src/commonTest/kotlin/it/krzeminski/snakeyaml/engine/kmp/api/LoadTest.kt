package it.krzeminski.snakeyaml.engine.kmp.api

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okio.Buffer

class LoadTest : FunSpec({
    test("string 'a' is parsed") {
        val load = Load()
        val str = load.loadOne("a")
        str shouldBe "a"
    }

    test("integer 1 is parsed") {
        val load = Load()
        val integer = load.loadOne("1")
        integer shouldBe 1
    }

    test("boolean true is parsed") {
        val load = Load()
        val bool = load.loadOne("true")
        bool shouldBe true
    }

    test("null is parsed") {
        val load = Load()
        val result = load.loadOne("")
        result shouldBe null
    }

    test("null tag is parsed") {
        val load = Load()
        val result = load.loadOne("!!null")
        result shouldBe null
    }

    test("float is parsed") {
        val load = Load()
        val doubleValue = load.loadOne("1.01")
        doubleValue shouldBe 1.01
    }

    test("load from string") {
        val load = Load()
        val v = load.loadOne("aaa")
        v shouldBe "aaa"
    }

    test("load from Source") {
        val load = Load()
        val v = load.loadOne(Buffer().write("bbb".encodeToByteArray()))
        v shouldBe "bbb"
    }

    test("load all from String") {
        val load = Load()
        val input = "bbb\n---\nccc\n---\nddd"
        val v = load.loadAll(input)
        val iter = v.iterator()
        iter.hasNext() shouldBe true
        iter.next() shouldBe "bbb"

        iter.hasNext() shouldBe true
        iter.next() shouldBe "ccc"

        iter.hasNext() shouldBe true
        iter.next() shouldBe "ddd"

        iter.hasNext() shouldBe false
    }

    test("load all from String as iterable") {
        val load = Load()
        val v = load.loadAll("1\n---\n2\n---\n3")
        var counter = 1
        for (o in v) {
            o shouldBe counter
            counter++
        }
        counter shouldBe 4
    }

    test("load all from String which has only 1 document") {
        val load = Load()

        val iterable = load.loadAll("1\n")
        var counter = 1
        for (o in iterable) {
            o shouldBe counter
            counter++
        }
        counter shouldBe 2

        val iter = load.loadAll("1\n").iterator()
        iter.hasNext() shouldBe true
        val o1 = iter.next()
        o1 shouldBe 1
        iter.hasNext() shouldBe false
    }

    test("load all from Source") {
        val load = Load()
        val v = load.loadAll(Buffer().write("bbb".encodeToByteArray()))
        val iter = v.iterator()
        iter.hasNext() shouldBe true
        val o1 = iter.next()
        o1 shouldBe "bbb"
        iter.hasNext() shouldBe false
    }

    test("load a lot of documents from the same Load instance (not recommended)") {
        val load = Load()
        repeat(100000) {
            val v = load.loadAll("{foo: bar, list: [1, 2, 3]}")
            val iter = v.iterator()
            iter.hasNext() shouldBe true
            val o1 = iter.next()
            o1 shouldNotBe null
            iter.hasNext() shouldBe false
        }
    }
})
