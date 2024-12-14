/*
 * Copyright (c) 2018, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package it.krzeminski.snakeyaml.engine.kmp.representer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.Dump
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.common.FlowStyle

/**
 * Test for issue
 * https://bitbucket.org/snakeyaml/snakeyaml-engine/issues/9/indentation-before-sequence
 */
internal class IndentationTest : FunSpec({

    fun createMap(): Map<*, *> = buildMap {
        put("key1", listOf("value1", "value2"))
        put("key2", listOf("value3", "value4"))
    }

    fun createSequence(): List<*> = buildList {
        add(buildMap<Any?, Any?> {
            put("key1", "value1")
            put("key2", "value2")
        })
        add(buildMap<Any?, Any?> {
            put("key3", "value3")
            put("key4", "value4")
        })
    }

    fun createDump(indicatorIndent: Int): Dump {
        val builder = DumpSettings.builder()
        builder.setDefaultFlowStyle(FlowStyle.BLOCK)
        builder.setIndicatorIndent(indicatorIndent)
        builder.setIndent(indicatorIndent + 2)
        val settings = builder.build()
        val dump = Dump(settings)
        return dump
    }

    test("Dump block map seq with default indent settings").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val dump = createDump(0)
        val output = dump.dumpToString(createMap())
        output shouldBe "key1:\n" + "- value1\n" + "- value2\n" + "key2:\n" + "- value3\n" + "- value4\n"
    }

    test("Dump block seq map with default indent settings").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val dump = createDump(0)
        val output = dump.dumpToString(createSequence())
        output shouldBe "- key1: value1\n" + "  key2: value2\n" + "- key3: value3\n" + "  key4: value4\n"
    }

    test("Dump block seq map with specified indicator indent").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val dump = createDump(2)
        val output = dump.dumpToString(createMap())
        output shouldBe "key1:\n" + "  - value1\n" + "  - value2\n" + "key2:\n" + "  - value3\n" + "  - value4\n"
    }

    test("Dump block seq map with indicatorIndent=2").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val dump = createDump(2)
        val output = dump.dumpToString(createSequence())
        output shouldBe "  - key1: value1\n" + "    key2: value2\n" + "  - key3: value3\n" + "    key4: value4\n"
    }
})
