@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.6.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:setup-java:v5")

import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.SetupJava.Distribution.Temurin
import io.github.typesafegithub.workflows.dsl.JobBuilder

fun JobBuilder<*>.setupJdk() =
    uses(
        name = "Set up Gradle Daemon JDK",
        action = SetupJava(
            javaVersion = "24",
            distribution = Temurin,
        ),
    )
