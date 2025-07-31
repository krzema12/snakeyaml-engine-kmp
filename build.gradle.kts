import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform`
    buildsrc.conventions.publishing
    buildsrc.conventions.`yaml-testing`
    buildsrc.conventions.`multiplatform-test-resources`
    alias(libs.plugins.dokkatoo.html)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

group = "it.krzeminski"
version = "3.1.2-SNAPSHOT"
description = "SnakeYAML Engine KMP"

apiValidation {
    ignoredProjects += listOf("snake-kmp-benchmarks")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.okio)
                implementation(libs.urlencoder.lib)
            }
        }

        commonTest {
            kotlin.srcDirs(
                tasks.yamlTestSuiteDataSources,
                tasks.convertCommonTestResourcesToKotlin,
            )

            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.jetbrains.annotations)
                // Overridig coroutines' version to solve a problem with WASM JS tests.
                // See https://kotlinlang.slack.com/archives/CDFP59223/p1736191408326039?thread_ts=1734964013.996149&cid=CDFP59223
                // TODO: remove this workaround in https://github.com/krzema12/snakeyaml-engine-kmp/issues/337
                runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.guava)
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
