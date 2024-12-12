package it.krzeminski.snakeyaml.engine.kmp.serializer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

data class TestClass(val value: String)

class IdentitySetTest : FunSpec({
    val objectFoo1 = TestClass(value = "foo")
    val objectFoo2 = TestClass(value = "foo")
    val objectBar = TestClass(value = "bar")

    test("expecting existence of the same object wrt. identity") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectFoo1) shouldBe true
    }

    test("comparing two objects that are equal according to 'equals'") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectFoo2) shouldBe false
    }

    test("comparing two objects that are not equal according to 'equals'") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectBar) shouldBe false
    }

    test("clearing the set") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectFoo1) shouldBe true
        identitySet.clear()
        identitySet.contains(objectFoo1) shouldBe false
    }

    test("removing the same object") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectFoo1) shouldBe true
        identitySet.remove(objectFoo1)
        identitySet.contains(objectFoo1) shouldBe false
    }

    test("removing object that is equal according to 'equals'") {
        val identitySet = IdentitySet<TestClass>()
        identitySet.add(objectFoo1)
        identitySet.contains(objectFoo1) shouldBe true
        identitySet.remove(objectFoo2)
        identitySet.contains(objectFoo1) shouldBe true
    }
})
