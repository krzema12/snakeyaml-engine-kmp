package it.krzeminski.snakeyaml.engine.kmp.usecases.env

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import it.krzeminski.snakeyaml.engine.kmp.api.LoadSettings
import it.krzeminski.snakeyaml.engine.kmp.env.EnvConfig
import it.krzeminski.snakeyaml.engine.kmp.exceptions.MissingEnvironmentVariableException
import it.krzeminski.snakeyaml.engine.kmp.internal.areEnvVarsSupported
import it.krzeminski.snakeyaml.engine.kmp.stringFromResources

class EnvVariableTest : FunSpec({
    // the variable is set by Gradle
    val VALUE1 = "EnvironmentValue1"
    val NULL_ENV_CONFIG = object : EnvConfig {
        override fun getValueFor(name: String, separator: String?, value: String?, environment: String?): String? {
            return null
        }
    }

    test("Parse docker-compose.yaml example").config(enabled = areEnvVarsSupported()) {
        val loader = Load(LoadSettings.builder().setEnvConfig(NULL_ENV_CONFIG).build())
        val resource = stringFromResources("/env/docker-compose.yaml")
        @Suppress("UNCHECKED_CAST")
        val compose = loader.loadOne(resource) as Map<String, Any>
        val output = compose.toString()
        output shouldEndWith "environment={URL1=EnvironmentValue1, URL2=, URL3=server3, URL4=, URL5=server5, URL6=server6}}}}"
    }

    test("Parsing ENV variables must be explicitly enabled") {
        val loader = Load()
        val loaded = loader.loadOne("\${EnvironmentKey1}") as String
        loaded shouldBe "\${EnvironmentKey1}"
    }

    fun load(template: String): String {
        val loader = Load(LoadSettings.builder().setEnvConfig(NULL_ENV_CONFIG).build())
        return loader.loadOne(template) as String
    }

    test("Parsing ENV variable which is defined and not empty").config(enabled = areEnvVarsSupported()) {
        load("\${EnvironmentKey1}") shouldBe VALUE1
        load("\${EnvironmentKey1-any}") shouldBe VALUE1
        load("\${EnvironmentKey1:-any}") shouldBe VALUE1
        load("\${EnvironmentKey1:?any}") shouldBe VALUE1
        load("\${EnvironmentKey1?any}") shouldBe VALUE1
    }

    test("Parsing ENV variable which is defined as empty").config(enabled = areEnvVarsSupported()) {
        load("\${EnvironmentEmpty}") shouldBe ""
        load("\${EnvironmentEmpty?}") shouldBe ""
        load("\${EnvironmentEmpty:-detected}") shouldBe "detected"
        load("\${EnvironmentEmpty-detected}") shouldBe ""
        load("\${EnvironmentEmpty?detectedError}") shouldBe ""
        shouldThrow<MissingEnvironmentVariableException> {
            load("\${EnvironmentEmpty:?detectedError}")
        }.also {
            it.message shouldBe "Empty mandatory variable EnvironmentEmpty: detectedError"
        }
    }

    test("Parsing ENV variable which is not set").config(enabled = areEnvVarsSupported()) {
        load("\${EnvironmentUnset}") shouldBe ""
        load("\${EnvironmentUnset:- }") shouldBe ""
        load("\${EnvironmentUnset:-detected}") shouldBe "detected"
        load("\${EnvironmentUnset-detected}") shouldBe "detected"
        shouldThrow<MissingEnvironmentVariableException> {
            load("\${EnvironmentUnset:?detectedError}")
        }.also {
            it.message shouldBe "Missing mandatory variable EnvironmentUnset: detectedError"
        }
        shouldThrow<MissingEnvironmentVariableException> {
            load("\${EnvironmentUnset?detectedError}")
        }.also {
            it.message shouldBe "Missing mandatory variable EnvironmentUnset: detectedError"
        }
    }
})
