package buildsrc.conventions.lang

import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

/** conventions for a Kotlin/JS subproject */

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
}

kotlin {
    js(IR) {
        browser()
        nodejs()
    }
}

afterEvaluate {
    rootProject.extensions.configure<YarnRootExtension> {
        // Kotlin/JS creates lockfiles for JS dependencies in the root directory.
        // I think it's a bit annoying to have a top-level directory for a single file, and it makes the project
        // a bit more crowded.
        // It's a little neater if the lock file dir is in the Gradle dir, next to the version catalog.
        lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")

        // produce an error if there's no lock file
        reportNewYarnLock = true
    }
}
