plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.lang.`kotlin-multiplatform-js`
    buildsrc.conventions.lang.`kotlin-multiplatform-native`
    buildsrc.conventions.`git-branch-publish`
    buildsrc.conventions.publishing
    buildsrc.conventions.`yaml-testing`
}

group = "org.snakeyaml"
version = "2.7-SNAPSHOT"
description = "SnakeYAML Engine KMP"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.squareup.okio:okio:3.3.0")
            }
        }

        commonTest {
            kotlin.srcDirs(tasks.yamlTestSuiteDataSources)

            dependencies {
                implementation(project.dependencies.platform("io.kotest:kotest-bom:5.6.2"))
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-assertions-core")
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("com.google.guava:guava:26.0-jre")
            }
        }
    }
}

tasks.matching { it.name.startsWith("copyResourcesDebugTest") }.configureEach {
    mustRunAfter(tasks.yamlTestSuiteDataSources)
}

tasks.withType<Test>().configureEach {
    environment(
        "EnvironmentKey1" to "EnvironmentValue1",
        "EnvironmentEmpty" to "",
    )
}
