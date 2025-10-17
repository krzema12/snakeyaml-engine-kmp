import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform`
    buildsrc.conventions.publishing
    buildsrc.conventions.`yaml-testing`
    buildsrc.conventions.`multiplatform-test-resources`
    alias(libs.plugins.dokkatoo.html)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

group = "it.krzeminski"
version = "4.0.0-SNAPSHOT"
description = "SnakeYAML Engine KMP"

apiValidation {
    ignoredProjects += listOf("snake-kmp-benchmarks")
}

dependencies {
    kspCommonMainMetadata(projects.copyDslKspProcessor)
}

kotlin {
    sourceSets {
        commonMain {
            // Not added by KSP by default.
            kotlin.srcDirs("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.okio)
                implementation(libs.urlencoder.lib)
                implementation(project(":copy-dsl-ksp-processor"))
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
                implementation(libs.kotest.assertions.table)
                implementation(libs.kotlinx.datetime)
                implementation(libs.jetbrains.annotations)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

// Not added by KSP by default.
tasks.withType<KotlinCompileCommon>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
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
