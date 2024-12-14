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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.krzeminski.snakeyaml.engine.kmp.api.DumpSettings
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node

internal class StandardRepresenterTest : FunSpec({
    val standardRepresenter = Representer(DumpSettings.builder().build())

    test("Represent unknown class").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        class MyCustomClass(val data: String)

        val exception: YamlEngineException = shouldThrow<YamlEngineException> {
            standardRepresenter.represent(
                MyCustomClass("test")
            )
        }
        exception.message shouldBe "Representer is not defined for class MyCustomClass"
    }

    test("Represent Enum as node with global tag").config(enabledOrReasonIf = identityHashCodeEnabledOrReasonIf) {
        val node: Node = standardRepresenter.represent(FormatEnum.JSON)
        node.tag.value shouldBe //        "tag:yaml.org,2002:it.krzeminski.snakeyaml.engine.kmp.representer.FormatEnum",
            "tag:yaml.org,2002:FormatEnum"
    }
})
