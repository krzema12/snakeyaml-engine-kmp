package it.krzeminski.snakeyaml.engine.kmp.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SystemTest : FunSpec({
    data class TestClass(val someField: String)

    test("identityHashCode -> different value for different objects") {
        val object1 = TestClass("Foo")
        val hashCode1 = identityHashCode(object1)
        val object2 = TestClass("Bar")
        val hashCode2 = identityHashCode(object2)
        hashCode1 shouldNotBe hashCode2
    }

    test("identityHashCode -> different value for different but equal objects") {
        val object1 = TestClass("Foo")
        val hashCode1 = identityHashCode(object1)
        val object2 = TestClass("Foo")
        val hashCode2 = identityHashCode(object2)
        object1 shouldBe object2
        hashCode1 shouldNotBe hashCode2
    }

    test("identityHashCode -> the same value for the same object") {
        val object1 = TestClass("Foo")
        val hashCode1 = identityHashCode(object1)
        val hashCode2 = identityHashCode(object1)
        hashCode1 shouldBe hashCode2
    }

    test("identityHashCode -> null always has same identity hash code") {
        val hashCode1 = identityHashCode(null)
        val hashCode2 = identityHashCode(null)
        hashCode1 shouldBe hashCode2
    }
})
