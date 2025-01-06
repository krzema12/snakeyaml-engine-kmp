import buildsrc.utils.configureGradleDaemonJvm
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform`
    buildsrc.conventions.publishing
    buildsrc.conventions.`git-branch-publish`
    buildsrc.conventions.`yaml-testing`
    buildsrc.conventions.`multiplatform-test-resources`
    id("dev.adamko.dokkatoo-html") version "2.4.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.17.0"
}

group = "it.krzeminski"
version = "3.1.1-SNAPSHOT"
description = "SnakeYAML Engine KMP"

apiValidation {
    ignoredProjects += listOf("snake-kmp-benchmarks")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.1")
                implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.6.0")
            }
        }

        commonTest {
            kotlin.srcDirs(
                tasks.yamlTestSuiteDataSources,
                tasks.convertCommonTestResourcesToKotlin,
            )

            dependencies {
                implementation("io.kotest:kotest-framework-engine:6.0.0.M1")
                implementation("io.kotest:kotest-framework-api:6.0.0.M1")
                implementation("io.kotest:kotest-assertions-core:6.0.0.M1")
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.11.4")
                implementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
                implementation("com.google.guava:guava:33.4.0-jre")
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    environment(
        "EnvironmentKey1" to "EnvironmentValue1",
        "EnvironmentEmpty" to "",
    )
}

tasks.withType<KotlinJsTest>().configureEach {
    environment("EnvironmentKey1", "EnvironmentValue1")
    environment("EnvironmentEmpty", "")
}

tasks.withType<KotlinNativeTest>().configureEach {
    environment("EnvironmentKey1", "EnvironmentValue1")
    environment("EnvironmentEmpty", "")
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_EnvironmentKey1", "EnvironmentValue1")
    environment("SIMCTL_CHILD_EnvironmentEmpty", "")
}

dokkatoo {
    moduleName = "SnakeYAML Engine KMP"

    val docsDir = layout.projectDirectory.dir("docs")

    dokkatooSourceSets.configureEach {
        includes.from(docsDir.file("modules.md"))

        externalDocumentationLinks.register("okio") {
            packageListUrl("https://square.github.io/okio/3.x/okio/okio/package-list")
            url("https://square.github.io/okio/3.x/okio/")
        }

        sourceLink {
            localDirectory = layout.projectDirectory
            val projectVersion = provider { project.version.toString() }
            remoteUrl = projectVersion.map { version ->
                // if project version matches SemVer, then point the URL to a tag - otherwise, point to main
                val remoteVersion =
                    if (version.matches(Regex("""[1-9][0-9]*\.[1-9][0-9]*\.[1-9][0-9]*"""))) {
                        "v${version}"
                    } else {
                        "main"
                    }
                uri("https://github.com/krzema12/snakeyaml-engine-kmp/blob/$remoteVersion")
            }
        }
    }

    pluginsConfiguration {
        html {
            homepageLink = "https://github.com/krzema12/snakeyaml-engine-kmp"
            customAssets.from(
                docsDir.files(
                    "img/homepage.svg",
                )
            )
        }
    }
}

configureGradleDaemonJvm(
    project = project,
    updateDaemonJvm = tasks.updateDaemonJvm,
    gradleDaemonJvmVersion = provider { JavaVersion.toVersion(21) },
)
