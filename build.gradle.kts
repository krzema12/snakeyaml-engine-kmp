plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.lang.`kotlin-multiplatform-js`
    buildsrc.conventions.lang.`kotlin-multiplatform-native`
    buildsrc.conventions.publishing
    buildsrc.conventions.`git-branch-publish`
    buildsrc.conventions.`yaml-testing`
    id("dev.adamko.dokkatoo-html") version "2.3.1"
}

group = "it.krzeminski"
version = "2.7.6-SNAPSHOT"
description = "SnakeYAML Engine KMP"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.squareup.okio:okio:3.9.0")
                implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.5.0")
            }
        }

        commonTest {
            kotlin.srcDirs(tasks.yamlTestSuiteDataSources)

            dependencies {
                implementation(project.dependencies.platform("io.kotest:kotest-bom:5.8.1"))
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-assertions-core")
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("com.google.guava:guava:33.1.0-jre")
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
