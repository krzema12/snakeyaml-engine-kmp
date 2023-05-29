#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:0.43.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV3
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV3
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Build",
    on = listOf(
        Push(branches = listOf("master")),
        PullRequest(),
    ),
    sourceFile = __FILE__.toPath(),
) {
    job(
        id = "build",
        runsOn = UbuntuLatest,
    ) {
        uses(CheckoutV3())
        uses(
            name = "Set up JDK",
            action = SetupJavaV3(
                javaVersion = "11",
                distribution = SetupJavaV3.Distribution.Zulu,
                cache = SetupJavaV3.BuildPlatform.Gradle,
            ),
        )
        uses(
            name = "Build",
            action = GradleBuildActionV2(
                arguments = "build",
            ),
        )
    }
}.writeToFile()
