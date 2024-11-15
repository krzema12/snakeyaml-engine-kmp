package it.krzeminski.snakeyaml.engine.kmp.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class SystemTest : FunSpec({
    context(::identityHashCode.name) {
        test("different value for different objects") {
            val object1 = "Foo"
            val hashCode1 = identityHashCode(object1)
            val object2 = "Bar"
            val hashCode2 = identityHashCode(object2)
            hashCode1 shouldNotBe hashCode2
        }
    }
})
