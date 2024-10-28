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
            name = "Clone snakeyaml-engine and check for changes",
            command = """
                git clone --branch master --single-branch https://bitbucket.org/snakeyaml/snakeyaml-engine.git
                cd snakeyaml-engine
                git log --oneline ef7ebe9c06e963e13f4ab465f300a0dd6f0940c9..master
            """.trimIndent()
        )
    }
}
