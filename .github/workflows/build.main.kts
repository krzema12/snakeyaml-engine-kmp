#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.6.0")

import io.github.typesafegithub.workflows.actions.actions.CacheV3
import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType.*
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Build",
    on = listOf(
        Push(branches = listOf("main")),
        PullRequest(),
    ),
    sourceFile = __FILE__.toPath(),
    concurrency = Concurrency(
        group = "\${{ github.workflow }} @ \${{ github.event.pull_request.head.label || github.head_ref || github.ref }}",
        cancelInProgress = true,
    ),
) {
    setOf(
        UbuntuLatest,
        MacOSLatest,
        WindowsLatest,
    ).forEach { jobRunner ->
        job(
            id = "build-on-${jobRunner::class.simpleName}",
            runsOn = jobRunner,
        ) {
            uses(action = CheckoutV4())
            uses(
                name = "Set up JDK",
                action = SetupJavaV3(
                    javaVersion = "11",
                    distribution = SetupJavaV3.Distribution.Zulu,
                    cache = SetupJavaV3.BuildPlatform.Gradle,
                ),
            )
            uses(
                name = "Cache Kotlin Konan",
                action = CacheV3(
                    path = listOf(
                        "~/.konan/**/*",
                    ),
                    key = "kotlin-konan-${expr { runner.os }}",
                ),
            )
            uses(
                name = "Build",
                action = GradleBuildActionV2(
                    arguments = "build",
                ),
            )
        }
    }
}.writeToFile()
