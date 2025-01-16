#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.1.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")
@file:DependsOn("nexus-actions:create-nexus-staging-repo:v1")
@file:DependsOn("nexus-actions:release-nexus-staging-repo:v1")
@file:DependsOn("nexus-actions:drop-nexus-staging-repo:v1")

import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Temurin
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.actions.nexusactions.CreateNexusStagingRepo
import io.github.typesafegithub.workflows.actions.nexusactions.DropNexusStagingRepo
import io.github.typesafegithub.workflows.actions.nexusactions.ReleaseNexusStagingRepo
import io.github.typesafegithub.workflows.domain.AbstractResult
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

val SONATYPE_USERNAME by Contexts.secrets
val SONATYPE_PASSWORD by Contexts.secrets
val SONATYPE_STAGING_PROFILE_ID by Contexts.secrets
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
) {
    val stagingRepoJob =
        job(
            id = "create-staging-repo",
            name = "Create staging repository",
            runsOn = RunnerType.UbuntuLatest,
            outputs = object : JobOutputs() {
                var repositoryId: String by output()
            }
        ) {
            val createRepo = uses(
                action = CreateNexusStagingRepo(
                    username = expr { SONATYPE_USERNAME },
                    password = expr { SONATYPE_PASSWORD },
                    stagingProfileId = expr { SONATYPE_STAGING_PROFILE_ID },
                )
            )
            jobOutputs.repositoryId = createRepo.outputs.repositoryId
        }
    val publishJob =
        job(
            id = "publish-artifacts",
            runsOn = RunnerType.MacOSLatest,
            needs = listOf(stagingRepoJob),
        ) {
            uses(action = Checkout())
            uses(
                name = "Set up Gradle Daemon JDK",
                action = SetupJava(
                    javaVersion = "21",
                    distribution = Temurin,
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
                name = "Set up Gradle",
                action = ActionsSetupGradle(
                    gradleVersion = "wrapper",
                ),
            )
            run(
                name = "Publish",
                command = "./gradlew publishAllPublicationsToSonatypeReleaseRepository --stacktrace",
                env =
                mapOf(
                    "ORG_GRADLE_PROJECT_snake-kmp.ossrhUsername" to expr { SONATYPE_USERNAME },
                    "ORG_GRADLE_PROJECT_snake-kmp.ossrhPassword" to expr { SONATYPE_PASSWORD },
                    "ORG_GRADLE_PROJECT_snake-kmp.ossrhStagingRepositoryId" to expr { stagingRepoJob.outputs.repositoryId },
                    "ORG_GRADLE_PROJECT_snake-kmp.signing.keyId" to expr { SIGNING_KEY_ID },
                    "ORG_GRADLE_PROJECT_snake-kmp.signing.key" to expr { SIGNING_KEY },
                    "ORG_GRADLE_PROJECT_snake-kmp.signing.password" to expr { SIGNING_PASSWORD },
                )
            )
        }

    job(
        id = "close-staging-repo",
        runsOn = RunnerType.UbuntuLatest,
        condition = expr { publishJob.result.eq(AbstractResult.Status.Success) },
        needs = listOf(stagingRepoJob, publishJob),
    ) {
        uses(
            action = ReleaseNexusStagingRepo(
                username = expr { SONATYPE_USERNAME },
                password = expr { SONATYPE_PASSWORD },
                stagingRepositoryId = expr { stagingRepoJob.outputs.repositoryId },
            )
        )
    }
    job(
        id = "drop-staging-repo",
        runsOn = RunnerType.UbuntuLatest,
        condition = expr { "!${cancelled()} && ${publishJob.result.neq(AbstractResult.Status.Success)}" },
        needs = listOf(stagingRepoJob, publishJob),
    ) {
        uses(
            action = DropNexusStagingRepo(
                username = expr { SONATYPE_USERNAME },
                password = expr { SONATYPE_PASSWORD },
                stagingRepositoryId = expr { stagingRepoJob.outputs.repositoryId },
            )
        )
    }
}
