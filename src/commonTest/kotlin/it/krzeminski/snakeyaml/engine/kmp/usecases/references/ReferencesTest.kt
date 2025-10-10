package it.krzeminski.snakeyaml.engine.kmp.usecases.references

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.common.Platform
import io.kotest.common.platform
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.concurrent.suspension.shouldCompleteBetween
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ReferencesTest : FunSpec({
    /**
     * Create data which is difficult to parse.
     *
     * @param size - size of the map, defines the complexity
     * @return YAML to parse
     */
    fun createDump(size: Int): String {
        val root: LinkedHashMap<Any?, Any?> = LinkedHashMap()
        var s1: LinkedHashMap<Any?, Any?>?
        var s2: LinkedHashMap<Any?, Any?>?
        var t1: LinkedHashMap<Any?, Any?>?
        var t2: LinkedHashMap<Any?, Any?>?
        s1 = root
        s2 = LinkedHashMap()
        /*
         * the time to parse grows very quickly SIZE -> time to parse in seconds 25 -> 1 26 -> 2 27 -> 3
         * 28 -> 8 29 -> 13 30 -> 28 31 -> 52 32 -> 113 33 -> 245 34 -> 500
         */
        for (i in 0..<size) {
            t1 = LinkedHashMap()
            t2 = LinkedHashMap()
            t1.put("foo", "1")
            t2.put("bar", "2")

            s1!!.put("a", t1)
            s1.put("b", t2)
            s2!!.put("a", t1)
            s2.put("b", t2)

            s1 = t1
            s2 = t2
        }

        // this is VERY BAD code
        // the map has itself as a key (no idea why it may be used except of a DoS attack)
        val f: LinkedHashMap<Any?, Any?> = LinkedHashMap()
        f.put(f, "a")
        f.put("g", root)

        val dump = Dump(DumpSettings())
        val output = dump.dumpToString(f)
        // TODO no replace should be needed
        return output.replace("001: ", "001 : ")
    }

    val failQuicklyTimeout = when(platform) {
        Platform.JS -> 4.seconds
        else -> 1.seconds
    }

    test("references with recursive keys not allowed by default") {
        val output = createDump(30)
        // Load
        val settings = LoadSettings(maxAliasesForCollections = 150)
        val load = Load(settings)
        val duration = measureTime {
            val ex = shouldThrow<Exception> {
                load.loadOne(output)
            }
            ex.message shouldBe "Recursive key for mapping is detected but it is not configured to be allowed."
        }
        withClue("It should fail quickly") {
            duration shouldBeLessThan failQuicklyTimeout
        }
    }

    test("Parsing with aliases may take a lot of time, CPU and memory") {
        val output = createDump(25)
        // Load
        val settings = LoadSettings(allowRecursiveKeys = true, maxAliasesForCollections = 50)
        val load = Load(settings)
        shouldCompleteBetween(400.milliseconds..15.seconds) {
            load.loadOne(output)
        }
    }

    test("Prevent DoS attack by failing early") {
        // without alias restriction this size should occupy tons of CPU, memory and time to parse
        val bigYAML = createDump(35)
        // Load
        val settings = LoadSettings(allowRecursiveKeys = true, maxAliasesForCollections = 40)
        val load = Load(settings)
        val duration = measureTime {
            val ex = shouldThrow<Exception> {
                load.loadOne(bigYAML)
            }
            ex.message shouldBe "Number of aliases for non-scalar nodes exceeds the specified max=40"
        }

        withClue("It should fail quickly") {
            duration shouldBeLessThan failQuicklyTimeout
        }
    }
})
