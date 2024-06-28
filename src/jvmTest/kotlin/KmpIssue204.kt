package it.krzeminski.snakeyaml.engine.kmp

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.resource.resourceAsString
import it.krzeminski.snakeyaml.engine.kmp.api.Load
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class KmpIssue204 : FunSpec({
    test("large yaml should be parsed quickly") {
        val openAiApiYaml = resourceAsString("/issues/kmp-issue-204-OpenAI-API.yaml")
        val duration = measureTime {
            val loader = Load()
            val value = loader.loadOne(openAiApiYaml) as? Map<*, *>
            value.shouldNotBeNull()
            value shouldHaveSize 8
        }
        duration shouldBeLessThan 500.milliseconds
    }
})
