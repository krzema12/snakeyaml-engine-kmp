#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.7.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v6")
@file:DependsOn("actions:cache:v6")
@file:DependsOn("gradle:actions__setup-gradle:v6")
@file:DependsOn("fwilhe2:setup-kotlin:v1")

@file:Import("setup-jdk.main.kts")

import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Temurin
import io.github.typesafegithub.workflows.actions.fwilhe2.SetupKotlin
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.DEFAULT_CONSISTENCY_CHECK_JOB_CONFIG

val SONATYPE_USERNAME by Contexts.secrets
val SONATYPE_PASSWORD by Contexts.secrets
val SIGNING_KEY_ID by Contexts.secrets
val SIGNING_KEY by Contexts.secrets
val SIGNING_PASSWORD by Contexts.secrets

workflow(
    name = "Publish release to Maven Central or snapshot repo",
    on = listOf(
        Push(branches = listOf("main")),
        WorkflowDispatch(),
    ),
    sourceFile = __FILE__,
    consistencyCheckJobConfig = DEFAULT_CONSISTENCY_CHECK_JOB_CONFIG.copy(
        additionalSteps = {
            uses(
                name = "Downgrade Kotlin",
                action = SetupKotlin(
                    // One version before 2.4.0 that contains a bug leading to this failure:
                    //   While analysing .github/workflows/build.main.kts:53:13:
                    //   org.jetbrains.kotlin.utils.exceptions.KotlinIllegalArgumentExceptionWithAttachments:
                    //   Expected FirResolvedTypeRef with ConeKotlinType but was FirUserTypeRefImpl
                    // This downgrade is meant to be a temporary workaround.
                    // See https://github.com/typesafegithub/github-workflows-kt/issues/2348
                    version = "2.3.21",
                ),
            )
        },
    ),
) {
    job(
        id = "release",
        runsOn = RunnerType.MacOSLatest,
    ) {
        uses(action = Checkout())
        setupJdk()
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
            name = "Set up Gradle",
            action = ActionsSetupGradle(
                gradleVersion = "wrapper",
            ),
        )
        run(
            name = "Publish",
            command = "./gradlew publishToMavenCentral --no-configuration-cache --stacktrace",
            env =
            mapOf(
                "ORG_GRADLE_PROJECT_mavenCentralUsername" to expr { SONATYPE_USERNAME },
                "ORG_GRADLE_PROJECT_mavenCentralPassword" to expr { SONATYPE_PASSWORD },
                "ORG_GRADLE_PROJECT_snake-kmp.signing.keyId" to expr { SIGNING_KEY_ID },
                "ORG_GRADLE_PROJECT_snake-kmp.signing.key" to expr { SIGNING_KEY },
                "ORG_GRADLE_PROJECT_snake-kmp.signing.password" to expr { SIGNING_PASSWORD },
            )
        )
    }
}
