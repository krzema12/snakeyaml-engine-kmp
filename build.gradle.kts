import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.8.21"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    testImplementation("com.google.guava:guava:26.0-jre")
}

group = "org.snakeyaml"
version = "2.7-SNAPSHOT"
description = "SnakeYAML Engine"
java.sourceCompatibility = JavaVersion.VERSION_1_8

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)

    target {
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_1_8)
                freeCompilerArgs.addAll(
                    "-Xjvm-default=all", // TODO remove this once everything is Kotlin
                )
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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


val regenWorkflows by tasks.registering(buildsrc.tasks.RegenerateWorkflowsKt::class) {
    workflowsDir.set(layout.projectDirectory.dir(".github/workflows/"))
}

tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
    // regenerate the workflows whenever IntelliJ is syncing
    // `:prepareKotlinIdeaImport` is a custom task that IntelliJ injects on-the-fly
    finalizedBy(regenWorkflows)
}
