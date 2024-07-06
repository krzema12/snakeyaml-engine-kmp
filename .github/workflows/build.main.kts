#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:2.2.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:gradle-build-action:v3")

import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Temurin
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Zulu
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildAction
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.RunnerType.*
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Build",
    on = listOf(
        Push(branches = listOf("main")),
        PullRequest(),
    ),
    sourceFile = __FILE__,
    concurrency = Concurrency(
        group = "\${{ github.workflow }} @ \${{ github.event.pull_request.head.label || github.head_ref || github.ref }}",
        cancelInProgress = true,
    ),
) {

    // Java 8 is the minimum supported version, and is used to run the tests.
    val minSupportedJava = "8"
    // Java 21 is to compile the project.
    val compiledWithJava = "21"

    setOf(
        UbuntuLatest,
        MacOSLatest,
        WindowsLatest,
    ).forEach { jobRunner ->
        job(
            id = "build-on-${jobRunner::class.simpleName}",
            runsOn = jobRunner,
        ) {
            uses(action = Checkout())
            uses(
                name = "Set up test launcher JDK",
                action = SetupJava(
                    javaVersion = minSupportedJava,
                    distribution = distributionFor(minSupportedJava),
                ),
            )
            uses(
                name = "Set up compilation JDK",
                action = SetupJava(
                    javaVersion = compiledWithJava,
                    distribution = distributionFor(compiledWithJava),
                ),
            )
            uses(
                name = "Cache Kotlin Konan",
                action = Cache(
                    path = listOf(
                        "~/.konan/**/*",
                    ),
                    key = "kotlin-konan-${expr { runner.os }}",
                ),
            )
            uses(
                name = "Build",
                action = GradleBuildAction(
                    arguments = "build",
                ),
            )
        }
    }
}

fun JobBuilder<*>.distributionFor(version: String): SetupJava.Distribution =
    if (runsOn == MacOSLatest && version.toInt() < 11) {
        // Temurin 8 isn't available on M1. Remove this when min Java version >= 11.
        Zulu
    } else {
        Temurin
    }
