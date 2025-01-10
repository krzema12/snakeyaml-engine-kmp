package it.krzeminski.snakeyaml.engine.kmp.usecases.recursive

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class RecursiveSetTest: FunSpec({
    test("Fail to load map with recursive keys") {
        val recursiveInput = stringFromResources("/recursive/recursive-set-1.yaml")
        val load = Load()
        shouldThrow< YamlEngineException> {
            load.loadOne(recursiveInput)
        }.also {
            it.message shouldBe "Recursive key for mapping is detected but it is not configured to be allowed."
        }
    }

    test("Load map with recursive keys if it is explicitly allowed") {
        val recursiveInput = stringFromResources("/recursive/recursive-set-1.yaml")
        val settings = LoadSettings.builder().setAllowRecursiveKeys(true).build()
        val load = Load(settings)
        @Suppress("UNCHECKED_CAST")
        val recursive = load.loadOne(recursiveInput) as Set<Any>
        recursive.size shouldBe 3
    }
})
