@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.usecases.colon_in_flow_context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import it.krzeminski.snakeyaml.engine.kmp.api.Load

class ColonInFlowContextInListTest : FunSpec({
    test("with spaces around") {
        val loader = Load()
        val list = loader.loadOne("[ http://foo ]") as List<String>
        list shouldContain "http://foo"
    }

    test("without spaces around") {
        val loader = Load()
        val list = loader.loadOne("[http://foo]") as List<String>
        list shouldContain "http://foo"
    }

    test("two values") {
        val loader = Load()
        val list = loader.loadOne("[ http://foo,http://bar ]") as List<String>
        list shouldContain "http://foo"
        list shouldContain "http://bar"
    }
})
