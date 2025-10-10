package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException

/**
 * https://en.wikipedia.org/wiki/Billion_laughs_attack#Variations
 */
class BillionLaughsAttackTest : FunSpec({
    test("load many aliases if explicitly allowed") {
        val settings = LoadSettings(maxAliasesForCollections = 72)
        val load = Load(settings)
        val map = load.loadOne(data) as Map<*, *>
        map.shouldNotBeNull()
    }

    test("billion laughs attack if data expanded") {
        val settings = LoadSettings(maxAliasesForCollections = 100)
        val load = Load(settings)
        val map = load.loadOne(data) as Map<*, *>
        map.shouldNotBeNull()

        if (platform != Platform.JVM) {
            // Asserting on running out of memory on platforms other than the JVM
            // is tricky, so let's skip this part of the test.
            return@test
        }

        val ex = shouldThrow<Throwable> {
            map.toString()
        }
        ex.message!! shouldContain "heap"
    }

    test("prevent billion laughs attack by default") {
        val load = Load()
        shouldThrowWithMessage<YamlEngineException>(
            message = "Number of aliases for non-scalar nodes exceeds the specified max=50",
        ) {
            load.loadOne(data)
        }
    }

    test("number of aliases for scalar nodes is not restricted") {
        // smaller than number of aliases for scalars
        val settings = LoadSettings(maxAliasesForCollections = 5)
        val load = Load(settings)
        load.loadOne(scalarAliasesData)
    }
})

private const val data = """a: &a ["lol","lol","lol","lol","lol","lol","lol","lol","lol"]
b: &b [*a,*a,*a,*a,*a,*a,*a,*a,*a]
c: &c [*b,*b,*b,*b,*b,*b,*b,*b,*b]
d: &d [*c,*c,*c,*c,*c,*c,*c,*c,*c]
e: &e [*d,*d,*d,*d,*d,*d,*d,*d,*d]
f: &f [*e,*e,*e,*e,*e,*e,*e,*e,*e]
g: &g [*f,*f,*f,*f,*f,*f,*f,*f,*f]
h: &h [*g,*g,*g,*g,*g,*g,*g,*g,*g]
i: &i [*h,*h,*h,*h,*h,*h,*h,*h,*h]"""

private const val scalarAliasesData = """a: &a foo
b:  *a
c:  *a
d:  *a
e:  *a
f:  *a
g:  *a
"""
