#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.0.1")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Check upstream",
    on = listOf(
        WorkflowDispatch(),
    ),
    sourceFile = __FILE__,
) {
    job(
        id = "check",
        runsOn = RunnerType.UbuntuLatest,
    ) {
        uses(action = Checkout())
        run(
            name = "Clone snakeyaml-engine",
            command = "git clone --branch master --single-branch https://bitbucket.org/snakeyaml/snakeyaml-engine.git"
        )
        run(
            name = "Enter snakeyaml-engine's dir",
            command = "cd snakeyaml-engine"
        )
        run(
            name = "DEBUG print wd",
            command = "pwd"
        )
        run(
            name = "DEBUG print ls",
            command = "ls"
        )
        run(
            name = "DEBUG run git status",
            command = "git status"
        )
        run(
            name = "DEBUG run git log",
            command = "git log"
        )
        run(
            name = "Calculate changes between the last synced change and current state",
            // TODO: store this commit hash in a file
            // TODO: get the numerical value (| wc -l)
            command = "git log --oneline ef7ebe9c06e963e13f4ab465f300a0dd6f0940c9..master"
        )
    }
}
