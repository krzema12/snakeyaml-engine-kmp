@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.issues.issue44

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.representer.StandardRepresenter
import kotlin.reflect.KClass

class ProtectedClassesTest : FunSpec({
    test("substitution") {
        val r = ExampleRepresenter(DumpSettings.builder().build())
        val node = r.represent(true)
        node.tag.value shouldBe "tag:yaml.org,2002:bool"
    }
})

private class ExampleRepresenter(settings: DumpSettings) : StandardRepresenter(settings) {
    init {
        this.representers.clear()
        this.representers[Boolean::class as KClass<Any>] = this.representBoolean
    }
}
