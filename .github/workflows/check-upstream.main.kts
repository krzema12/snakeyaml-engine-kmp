#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.6.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v6")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.workflow

val numberOfCommitsFileName = "number-of-commits.txt"
val logDiffBetweenRepos = "log-diff-between-repos.txt"
val badgeFileName = "commits-to-upstream-badge.svg"

workflow(
    name = "Check upstream",
    on = listOf(
        WorkflowDispatch(),
        Push(branches = listOf("main")),
        Schedule(triggers = listOf(Cron(minute = "0", hour = "0", dayWeek = "6"))), // Once a week.
    ),
    sourceFile = __FILE__,
) {
    job(
        id = "check",
        runsOn = RunnerType.UbuntuLatest,
    ) {
        uses(action = Checkout(ref = "commits-to-upstream-badge"))
        run(
            name = "Clone snakeyaml-engine and check for changes",
            command = """
                git clone --branch master --single-branch https://bitbucket.org/snakeyaml/snakeyaml-engine.git
                wget https://raw.githubusercontent.com/krzema12/snakeyaml-engine-kmp/${'$'}{{ github.ref }}/latest-analyzed-upstream-commit.txt
                cd snakeyaml-engine
                UPSTREAM_COMMIT=$(cat ../latest-analyzed-upstream-commit.txt)
                echo "See in BitBucket: https://bitbucket.org/snakeyaml/snakeyaml-engine/branches/compare/master..${'$'}UPSTREAM_COMMIT" > ../$logDiffBetweenRepos
                echo "" >> ../$logDiffBetweenRepos
                git log --oneline ${'$'}UPSTREAM_COMMIT..master >> ../$logDiffBetweenRepos
                git log --oneline ${'$'}UPSTREAM_COMMIT..master | wc -l > ../$numberOfCommitsFileName
            """.trimIndent(),
        )
        run(
            name = "Create an SVG with the number of commits",
            command = "wget -O $badgeFileName https://img.shields.io/badge/Not%20analyzed%20from%20upstream-$(cat $numberOfCommitsFileName)-blue",
        )
        run(
            name = "Preview badge",
            command = "cat $badgeFileName",
        )
        run(
            name = "Preview log diff",
            command = "cat $logDiffBetweenRepos",
        )
        run(
            name = "Commit updated badge and log diff",
            command = """
                git config --global user.email "<>"
                git config --global user.name "GitHub Actions Bot"

                git add $badgeFileName
                git add $logDiffBetweenRepos
                git commit --allow-empty -m "Regenerate badge and log diff"
                git push
            """.trimIndent()
        )
    }
}
