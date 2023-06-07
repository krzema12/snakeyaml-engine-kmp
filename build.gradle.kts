import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    buildsrc.conventions.lang.`kotlin-multiplatform-js`
    // Kotlin/Native is disabled for now because it will make the build slower during this phase of development.
    // Native target can be enabled when the project is more stable (after all code is KMP).
    //buildsrc.conventions.lang.`kotlin-multiplatform-native`
}

group = "org.snakeyaml"
version = "2.7-SNAPSHOT"
description = "SnakeYAML Engine"

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.squareup.okio:okio:3.3.0")
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.4.0")
                implementation("com.google.guava:guava:26.0-jre")
            }
        }

        jsTest {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:5.6.2")
                implementation("io.kotest:kotest-framework-api:5.6.2")
                implementation("io.kotest:kotest-assertions-core-js:5.6.2")
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STANDARD_OUT,
        )
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
    environment(
        "EnvironmentKey1" to "EnvironmentValue1",
        "EnvironmentEmpty" to "",
    )
}
