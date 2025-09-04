package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load

class RestrictAliasNamesTest : FunSpec({
    test("alias from Ruby") {
        val yamlProcessor = Load()
        shouldThrow<Exception> {
            yamlProcessor.loadOne("Exclude: **/*_old.rb")
        }.also { exception ->
            exception.message shouldContain "unexpected character found *(42)"
        }
    }
})
