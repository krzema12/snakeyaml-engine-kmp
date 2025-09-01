package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * https://en.wikipedia.org/wiki/Billion_laughs_attack#Variations
 */
class BillionLaughsAttackTest : FunSpec({
    test("load many aliases if explicitly allowed") {
        val settings = LoadSettings.builder().setMaxAliasesForCollections(72).build()
        val load = Load(settings)
        val map = load.loadOne(data) as Map<*, *>
        map shouldNotBe null
    }

    test("billion laughs attack if data expanded") {
        val settings = LoadSettings.builder().setMaxAliasesForCollections(100).build()
        val load = Load(settings)
        val map = load.loadOne(data) as Map<*, *>
        map shouldNotBe null
        shouldThrow<Throwable> {
            map.toString()
        }.also { exception ->
            exception.message shouldContain "heap"
        }
    }

    test("prevent billion laughs attack by default") {
        val load = Load()
        shouldThrow<YamlEngineException> {
            load.loadOne(data)
        }.also { exception ->
            exception.message shouldBe "Number of aliases for non-scalar nodes exceeds the specified max=50"
        }
    }

    test("number of aliases for scalar nodes is not restricted") {
        // smaller than number of aliases for scalars
        val settings = LoadSettings.builder().setMaxAliasesForCollections(5).build()
        val load = Load(settings)
        load.loadOne(scalarAliasesData)
    }
})

private val data = "a: &a [\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\",\"lol\"]\n" +
        "b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]\n" +
        "c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]\n" +
        "d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]\n" +
        "e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]\n" +
        "f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]\n" +
        "g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]\n" +
        "h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]\n" +
        "i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]"

private val scalarAliasesData = "a: &a foo\n" +
        "b:  *a\n" +
        "c:  *a\n" +
        "d:  *a\n" +
        "e:  *a\n" +
        "f:  *a\n" +
        "g:  *a\n"
