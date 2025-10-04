@file:Suppress("UNCHECKED_CAST")
package it.krzeminski.snakeyaml.engine.kmp.issues.issue11

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings

class TabInFlowContextTest : FunSpec({
    test("do not fail to parse if TAB is used (issue 11)") {
        val loadSettings = LoadSettings()
        val input = "{\n\t\"x\": \"y\"\n}"
        val obj = Load(loadSettings).loadOne(input)
        val map = obj as Map<String, Any?>
        map["x"] shouldBe "y"
    }

    test("TAB cannot start a token") {
        val loadSettings = LoadSettings()
        shouldThrow<Exception> {
            Load(loadSettings).loadOne("\t  data: 1")
        }.also {
            it.message shouldBe "while scanning for the next token\n" +
                "found character '\\t(TAB)' that cannot start any token. " +
                "(Do not use \\t(TAB) for indentation)\n" + " in reader, " +
                "line 1, column 1:\n    \t  data: 1\n" + "    ^\n"
        }
    }

    test("issue 55") {
        val loadSettings = LoadSettings()
        val obj = Load(loadSettings).loadOne("{ \"foo\":\t\"bar\" }")
        obj.shouldNotBeNull()
    }
})
