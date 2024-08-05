#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:2.3.0")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:download-artifact:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:actions__wrapper-validation:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")
@file:DependsOn("benchmark-action:github-action-benchmark:v1")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.DownloadArtifact
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.benchmarkaction.GithubActionBenchmark
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.actions.gradle.ActionsWrapperValidation
import io.github.typesafegithub.workflows.annotations.ExperimentalKotlinLogicStep
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import java.io.File

val BENCHMARK_RESULTS = "snake-kmp-benchmarks/build/reports/benchmarks"
val RESULTS_DIR = "bench-results"
val AGGREGATED_REPORT = "aggregated.json"
val PUBLISH_BENCHMARK_RESULTS by Contexts.secrets

workflow(
    name = "Run Benchmarks",
    sourceFile = __FILE__,
    on = listOf(
        Push(branches = listOf("main")),
        PullRequest()
    ),
    concurrency = Concurrency(
        group = "${expr { github.workflow }} @ ${expr { "${github.eventPullRequest.pull_request.head.label} || ${github.head_ref} || ${github.ref}" }}",
        cancelInProgress = true,
    )
) {
    val runBenchmark = job(
        id = "run-benchmark",
        name = "Performance regression check on ${'$'}{{ matrix.os }} runner",
        runsOn = RunnerType.Custom("${'$'}{{ matrix.os }}"),
        _customArguments = mapOf(
            "strategy" to mapOf(
                "fail-fast" to true,
                "matrix" to mapOf(
                    "include" to listOf(
                        mapOf("os" to "ubuntu-latest"),
                        mapOf(
                            "os" to "macos-latest",
                            "additional-args" to "-x jvmBenchmark -x jsBenchmark",
                        ),
                        mapOf(
                            "os" to "macos-13", // for macosX64
                            "additional-args" to "-x jvmBenchmark -x jsBenchmark",
                        ),
                        mapOf(
                            "os" to "windows-latest",
                            "additional-args" to "-x jvmBenchmark -x jsBenchmark",
                        ),
                    )
                )
            )
        )
    ) {
        uses(action = Checkout())
        uses(
            name = "Set up JDK",
            action = SetupJava(
                javaVersion = "11",
                distribution = SetupJava.Distribution.Zulu,
                cache = SetupJava.BuildPlatform.Gradle,
            ),
        )
        uses(
            name = "Validate Gradle Wrapper",
            action = ActionsWrapperValidation(),
        )
        uses(
            name = "Setup Gradle",
            action = ActionsSetupGradle(
                gradleVersion = "wrapper",
            ),
        )
        run(
            name = "Run benchmarks",
            command = "./gradlew -p snake-kmp-benchmarks benchmark --no-parallel ${ expr{ "matrix.additional-args" }}",
        )
        uses(
            action = UploadArtifact(
                name = "bench-results-${ expr { "matrix.os" } }",
                path = listOf("$BENCHMARK_RESULTS/main/**/*.json"),
            )
        )
    }

    job(
        id = "collect-benchmarks-results",
        runsOn = RunnerType.UbuntuLatest,
        needs = listOf(runBenchmark),
    ) {
        // without checkout step 'benchmark-action/github-action-benchmark' action won't work
        uses(action = Checkout())
        uses(
            name = "Download benchmark results",
            action = DownloadArtifact(
                pattern = "bench-results-*",
                path = RESULTS_DIR,
                mergeMultiple = true,
            )
        )
        @OptIn(ExperimentalKotlinLogicStep::class)
        run(
            name = "Prepare and join benchmark reports",
        ) {
            val mergedReports = File(RESULTS_DIR).walk()
                .filter { it.extension == "json" }
                .flatMap {
                    val reportForPlatform = it.readText()
                        // Trim lengthy Kotlin package and data file path to make benchmark name more readable
                        .replace("it.krzeminski.snakeyaml.engine.kmp.benchmark", it.nameWithoutExtension)
                        .replace(Regex("\"[^\"]+data"), "\"data")
                    Json.parseToJsonElement(reportForPlatform).jsonArray
                }
                .toList()
            val mergedReport = JsonArray(mergedReports).toString()
            File(AGGREGATED_REPORT).writeText(mergedReport)
        }
        uses(
            name = "Store benchmark result",
            action = GithubActionBenchmark(
                name = "SnakeKMP benchmarks",
                tool = GithubActionBenchmark.Tool.Jmh,
                commentOnAlert = true,
                summaryAlways = true,
                alertThreshold = "150%",
                failThreshold = "200%",
                ghRepository = "github.com/krzema12/snakeyaml-engine-kmp-benchmarks",
                outputFilePath = AGGREGATED_REPORT,
                _customInputs = mapOf(
                    "github-token" to expr { PUBLISH_BENCHMARK_RESULTS },
                    // Push and deploy GitHub pages branch automatically only if run in main repo and not in PR
                    "auto-push" to expr { "${github.repository} == 'krzema12/snakeyaml-engine-kmp' && ${github.event_name} != 'pull_request'" },
                )
            ),
        )
    }
}
