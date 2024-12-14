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
import it.krzeminski.snakeyaml.engine.kmp.common.ScalarStyle
import it.krzeminski.snakeyaml.engine.kmp.nodes.ScalarNode

internal class EnumRepresenterTest : FunSpec({
    test("Represent Enum as node with global tag").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val settings =
            DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build()
        val standardRepresenter: Representer = Representer(settings)
        val node =
            standardRepresenter.represent(FormatEnum.JSON) as ScalarNode
        node.scalarStyle shouldBe ScalarStyle.DOUBLE_QUOTED
        node.tag.value shouldBe
            //        "tag:yaml.org,2002:it.krzeminski.snakeyaml.engine.kmp.representer.FormatEnum",
            "tag:yaml.org,2002:FormatEnum"
    }

    test("Dump Enum with ScalarStyle.DOUBLE_QUOTED").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val settings =
            DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED).build()
        val dumper = Dump(settings)
        val node = dumper.dumpToString(FormatEnum.JSON)
        // node shouldBe "!!it.krzeminski.snakeyaml.engine.kmp.representer.FormatEnum \"JSON\"\n"
        node shouldBe "!!FormatEnum \"JSON\"\n"
    }
})
