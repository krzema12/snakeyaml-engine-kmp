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
package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings.Companion.builder
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class EnvVariableJvmTest : FunSpec({
    // the variables EnvironmentKey1 and EnvironmentEmpty are set by Gradle
    val KEY1 = "EnvironmentKey1"
    val EMPTY = "EnvironmentEmpty"
    val VALUE1 = "EnvironmentValue1"

    /**
     * This is a JVM-specific test, exercising system properties.
     */
    test("Custom EVN config example") {
        val provided = mapOf(KEY1 to "VVVAAA111")
        System.setProperty(EMPTY, "VVVAAA222")
        val loader = Load(
            builder().setEnvConfig(
                CustomEnvConfig(
                    provided
                )
            ).build()
        )
        val resource = stringFromResources("/env/docker-compose.yaml")
        @Suppress("UNCHECKED_CAST")
        val compose = loader.loadOne(resource) as Map<String, Any>?
        val output = compose.toString()
        output shouldEndWith "environment={URL1=VVVAAA111, URL2=VVVAAA222, URL3=VVVAAA222, URL4=VVVAAA222, URL5=server5, URL6=server6}}}}"
    }

    /**
     * This is a JVM-specific test, exercising environment variables.
     */
    test("environment set") {
        withClue("Gradle must set the variable") {
            System.getenv(KEY1) shouldBe VALUE1
        }
        withClue("Gradle must set the variable") {
            System.getenv(EMPTY) shouldBe ""
        }
    }
})
