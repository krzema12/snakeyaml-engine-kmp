import buildsrc.utils.configureGradleDaemonJvm

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform`
    buildsrc.conventions.publishing
    buildsrc.conventions.`git-branch-publish`
    buildsrc.conventions.`yaml-testing`
    id("dev.adamko.dokkatoo-html") version "2.4.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3"
}

group = "it.krzeminski"
version = "3.0.3"
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
            kotlin.srcDirs(tasks.yamlTestSuiteDataSources)

            dependencies {
                implementation(project.dependencies.platform("io.kotest:kotest-bom:5.9.1"))
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-assertions-core")
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("com.google.guava:guava:33.3.1-jre")
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
