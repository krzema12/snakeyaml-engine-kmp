import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform-jvm`
    // only target JVM, for now...
    //buildsrc.conventions.lang.`kotlin-multiplatform-js`
    //buildsrc.conventions.lang.`kotlin-multiplatform-native`
}

group = "org.snakeyaml"
version = "2.7-SNAPSHOT"
description = "SnakeYAML Engine"

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.addAll(
                    "-Xjvm-default=all", // TODO remove this once everything is Kotlin
                )
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                //implementation("com.squareup.okio:okio:3.3.0") // TODO https://github.com/krzema12/snakeyaml-engine-kmp/issues/42
            }
        }

        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-engine:5.4.0")
                implementation("com.google.guava:guava:26.0-jre")
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
