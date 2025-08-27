package it.krzeminski.snakeyaml.engine.kmp.issues.issue44

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter
import kotlin.reflect.KClass

private class ExampleRepresenter(settings: DumpSettings) : StandardRepresenter(settings) {
    init {
        this.representers.clear()
        this.representers[Boolean::class as KClass<Any>] = this.representBoolean
    }
}

class ProtectedClassesTest : FunSpec({
    test("Substitution") {
        val r = ExampleRepresenter(DumpSettings.builder().build())
        1 shouldBe 1
    }
})
