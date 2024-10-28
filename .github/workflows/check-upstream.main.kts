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
                git log --oneline $(cat ../upstream-commit.txt)..master | wc -l > ../number-of-commits.txt
            """.trimIndent()
        )
        run(
            name = "Create an SVG with the number of commits",
            command = """
                echo "<svg viewBox=\"0 0 240 80\" xmlns=\"http://www.w3.org/2000/svg\"><text x=\"20\" y=\"35\">${'$'}(cat number-of-commits.txt)</text></svg>" > badge.svg
            """.trimIndent()
        )
        run(command = "cat badge.svg")
    }
}
