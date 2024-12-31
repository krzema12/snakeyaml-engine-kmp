package it.krzeminski.snakeyaml.engine.kmp.internal

import io.kotest.assertions.throwables.shouldThrow
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
        hashCode1 shouldBe IdentityHashCode(0)
    }

    listOf(
        1.toByte(),
        1.toShort(),
        1.toInt(),
        1.toLong(),
        1.5.toFloat(),
        1.5.toDouble(),
        "test",
        'a',
    ).forEach { primitive ->
        val type = primitive::class.simpleName
        test("identityHashCode -> primitive type $type causes exception") {
            val ex = shouldThrow<IllegalArgumentException> {
                identityHashCode(primitive)
            }
            ex.message shouldBe
                "identity hash code cannot be computed for primitives and strings (type: $type)"
        }
    }
})
