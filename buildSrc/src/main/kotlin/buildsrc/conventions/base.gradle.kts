package buildsrc.conventions

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/** common config for all subprojects */

plugins {
    base
}

if (project != rootProject) {
    project.version = rootProject.version
    project.group = rootProject.group
}

tasks.withType<AbstractArchiveTask>().configureEach {
    // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}


tasks.withType<AbstractCopyTask>().configureEach {
    // When copy/sync tasks move or exclude files Gradle will retain redundant directories, even if
    // they're empty. This is usually useless, so by default tell Gradle to remove empty dirs.
    includeEmptyDirs = false
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
}
