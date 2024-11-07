#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.0.1")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:OptIn(ExperimentalKotlinLogicStep::class)

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.Checkout.FetchDepth.Infinite
import io.github.typesafegithub.workflows.annotations.ExperimentalKotlinLogicStep
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.workflow
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

val badgeFileName = "commits-to-upstream-badge.svg"

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
        uses(action = Checkout(fetchDepth = Infinite))
        run(
            name = "Clone snakeyaml-engine and check for changes",
            command = """
                git clone --branch master --single-branch https://bitbucket.org/snakeyaml/snakeyaml-engine.git
                cd snakeyaml-engine
                git log --oneline $(cat ../upstream-commit.txt)..master | wc -l > ../number-of-commits.txt
            """.trimIndent(),
        )
        run(
            name = "Create an SVG with the number of commits",
        ) {
            val numberOfCommits = Path("number-of-commits.txt").readText().trim()
            val badgeAsSvg = """
                <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="128" height="20" role="img" aria-label="To upstream: $numberOfCommits">
                  <title>To upstream: $numberOfCommits</title>
                  <linearGradient id="s" x2="0" y2="100%">
                    <stop offset="0" stop-color="#bbb" stop-opacity=".1"/>
                    <stop offset="1" stop-opacity=".1"/>
                  </linearGradient>
                  <clipPath id="r">
                    <rect width="128" height="20" rx="3" fill="#fff"/>
                  </clipPath>
                  <g clip-path="url(#r)">
                    <rect width="89" height="20" fill="#555"/>
                    <rect x="89" width="39" height="20" fill="#4cc"/>
                    <rect width="128" height="20" fill="url(#s)"/>
                  </g>
                  <g fill="#fff" text-anchor="middle" font-family="Verdana,Geneva,DejaVu Sans,sans-serif" text-rendering="geometricPrecision" font-size="110">
                    <text aria-hidden="true" x="455" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)" textLength="790">To upstream</text>
                    <text x="455" y="140" transform="scale(.1)" fill="#fff" textLength="790">To upstream</text>
                    <text aria-hidden="true" x="1075" y="150" fill="#010101" fill-opacity=".3" transform="scale(.1)">$numberOfCommits</text>
                    <text x="1075" y="140" transform="scale(.1)" fill="#fff">$numberOfCommits</text>
                  </g>
                </svg>
            """.trimIndent()
            Path(badgeFileName).writeText(badgeAsSvg)
        }
        run(
            name = "Preview badge",
            command = "cat $badgeFileName",
        )
        run(
            name = "Commit updated badge",
            command = """
                git checkout commits-to-upstream-badge
                git config --global user.email "<>"
                git config --global user.name "GitHub Actions Bot"
                git add $badgeFileName
                git commit --allow-empty -m "Regenerate badge"
                git push
            """.trimIndent()
        )
    }
}
