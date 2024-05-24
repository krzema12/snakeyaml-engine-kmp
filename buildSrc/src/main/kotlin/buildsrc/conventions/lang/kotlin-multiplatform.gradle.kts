package buildsrc.conventions.lang

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

/**
 * Base configuration for all Kotlin/Multiplatform conventions.
 *
 * This plugin does not enable any Kotlin target. To enable a target in a subproject, prefer applying specific Kotlin
 * target convention plugins.
 */

plugins {
    id("buildsrc.conventions.base")
    kotlin("multiplatform")
    id("io.kotest.multiplatform")
}


@OptIn(
    ExperimentalKotlinGradlePluginApi::class,
    ExperimentalWasmDsl::class,
)
kotlin {
    jvmToolchain(11)

    //region JVM Targets
    jvm {
        withJava()
    }
    //endregion


    //region JS Targets
    js(IR) {
        browser()
        nodejs()
    }
    //endregion


    //region Wasm Targets
    wasmJs {
        browser()
        nodejs()
    }

    //wasmWasi {
    //    nodejs()
    //}
    //endregion


    //region Native Targets
    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    mingwX64()
    //endregion

    applyDefaultHierarchyTemplate()

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes"
        )
    }

    // configure all Kotlin/JVM Tests to use JUnit
    targets.withType<KotlinJvmTarget>().configureEach {
        testRuns.configureEach {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
}

// `kotlin-js` and `kotlin-multiplatform` plugins adds a directory in the root-dir for the Yarn lockfile.
// I think it's a bit annoying to have a top-level directory for a single file, and it makes the project
// a bit more crowded.
// It's a little neater if it's in the Gradle dir, next to the version catalog.
afterEvaluate {
    rootProject.extensions.configure<NpmExtension> {
        lockFileDirectory = project.rootDir.resolve("gradle/kotlin-js-store")
    }
}


//region FIXME: WORKAROUND https://youtrack.jetbrains.com/issue/KT-65864
// Use a Node.js version current enough to support Kotlin/Wasm

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        // Initialize once in a multi-project build.
        // Otherwise, Gradle would complain "Configuration already finalized for previous property values".
        if (!System.getProperty("nodeJsCanaryConfigured").toBoolean()) {
            version = "22.0.0-nightly2024010568c8472ed9"
            logger.lifecycle("Using Node.js $version to support Kotlin/Wasm")
            downloadBaseUrl = "https://nodejs.org/download/nightly"
            System.setProperty("nodeJsCanaryConfigured", "true")
        }
    }
}

//rootProject.tasks.withType<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>().configureEach {
//    args.add("--ignore-engines") // Prevent Yarn from complaining about newer Node.js versions.
//}
//endregion
