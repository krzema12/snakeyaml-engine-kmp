package it.krzeminski.snakeyaml.engine.kmp.issues.issue46

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load

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

        parse("\u2029*", "\n\u2029* ") // empty alias
        parse("\u2029*", "\n\u2029*") // empty alias
        parse("\u2029* 1", "\n\u2029* 1")
    }

    test("parse document where 2028 is used as leading space (3rd)") {
        val load = Load()
        val docs = load.loadAll("--- |2-\n\n\u2028  * C\n")
        docs.shouldNotBeNull()
        val doc = docs.iterator().next()
        doc.shouldNotBeNull()
    }

    test("parse document") {
        val load = Load()
        val obj = load.loadAll("--- |2-\n\n  \u2028* C\n")
        obj.shouldNotBeNull()
        val doc = docs.iterator().next()
        doc shouldBe "\n\u2028* C"
    }

    test("* is not alias after 2028") {
        val load = Load()
        val obj = load.loadAll("\n\u2028* C")
        for (o in obj) {
            o shouldBe "\u2028* C"
        }
    }

    test("use anchor instead of alias") {
        val load = Load()
        val obj = load.loadAll("\n\u2028&C")
        for (o in obj) {
            o shouldBe "\u2028&C"
        }
    }
})

private fun parse(expected: Any, data: String) {
    val load = Load()
    val obj = load.loadOne(data)
    obj shouldBe expected
}
