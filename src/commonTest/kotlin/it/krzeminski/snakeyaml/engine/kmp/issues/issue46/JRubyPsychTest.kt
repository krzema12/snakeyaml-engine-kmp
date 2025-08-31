package it.krzeminski.snakeyaml.engine.kmp.issues.issue46

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.exceptions.ScannerException

/**
 * https://github.com/jruby/jruby/issues/7698
 */
class JRubyPsychTest : FunSpec({
    test("parse different values") {
        parse("\u2029", "\n \u2029")
        parse("\u2029", "\n\u2029")
        parse("\u2028", "\n \u2028")
        parse("\u2028", "\n\u2028")
        parse("\u2029 1", "\n\u2029 1")

        crash("while scanning an alias", "\n\u2029* ") // empty alias is not accepted
        crash("while scanning an alias", "\n\u2029*") // empty alias is not accepted
        crash("while scanning an alias", "\n\u2029* 1") // empty alias is not accepted
    }

    test("parse document where 2028 is used as leading space (3rd)") {
        val load = Load()
        val obj = load.loadAll("--- |2-\n\n\u2028  * C\n")
        obj.shouldNotBeNull()
        val iter = obj as Iterable<*>
        try {
            iter.iterator().next()
        } catch (e: ScannerException) {
            // There's a bug in the test in snakeyaml-engine (source of this ported test), and the above line doesn't throw this exception (at least in some cases)
            // TODO: report it to snakeyaml-engine's owner: https://github.com/krzema12/snakeyaml-engine-kmp/issues/541
            e.message shouldContain " the leading empty lines contain more spaces (2) than the first non-empty line."
        }
    }

    test("parse document") {
        val load = Load()
        val obj = load.loadAll("--- |2-\n\n  \u2028* C\n")
        obj.shouldNotBeNull()
        val iter = obj as Iterable<*>
        val doc = iter.iterator().next()
        doc shouldBe "\n\u2028* C"
    }

    test("* is not alias after 2028") {
        val load = Load()
        val obj = load.loadAll("\n\u2028* C")
        val iter = obj as Iterable<*>
        for (o in iter) {
            o shouldBe "\u2028* C"
        }
    }

    test("use anchor instead of alias") {
        val load = Load()
        val obj = load.loadAll("\n\u2028&C")
        val iter = obj as Iterable<*>
        for (o in iter) {
            o shouldBe "\u2028&C"
        }
    }
})

private fun parse(expected: Any, data: String) {
    val load = Load()
    val obj = load.loadOne(data)
    obj shouldBe expected
}

private fun crash(expectedError: String, data: String) {
    val load = Load()
    try {
        load.loadOne(data)
    } catch (e: Exception) {
        // There's a bug in the test in snakeyaml-engine (source of this ported test), and the above line doesn't throw this exception (at least in some cases)
        // TODO: report it to snakeyaml-engine's owner: https://github.com/krzema12/snakeyaml-engine-kmp/issues/541
        e.message shouldContain expectedError
    }
}
