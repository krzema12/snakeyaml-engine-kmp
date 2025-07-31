package buildsrc.conventions.lang

import buildsrc.utils.JavaLanguageVersion
import buildsrc.utils.JvmTarget
import buildsrc.utils.compilerFor
import buildsrc.utils.launcherFor
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
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
    jvmToolchain(21)

    //region JVM Targets
    jvm {
        withJava()
    }
    //endregion


    //region JS Targets
    js(IR) {
        binaries.library()
        browser()
        nodejs()
    }
    //endregion


    //region Wasm Targets
    wasmJs {
        binaries.library()
        browser()
        nodejs()
    }

    // Disable Wasi: No matching variant of io.kotest:kotest-framework-engine:5.9.0 was found
    //wasmWasi {
    //    binaries.library()
    //    //nodejs()
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

    applyDefaultHierarchyTemplate {
        common {
            group("jsCommon") {
                withJs()
                group("wasm") {
                    withWasmJs()
                    withWasmWasi()
                }
            }
        }
    }

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

//region Java versioning
val minSupportedJavaVersion = JavaVersion.VERSION_11

// use Java 21 to compile the project
val javaCompiler = javaToolchains.compilerFor(21)
// use minimum Java version to run the tests
val javaTestLauncher = javaToolchains.launcherFor(minSupportedJavaVersion)

kotlin.targets.withType<KotlinJvmTarget>().configureEach {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        jvmTarget = JvmTarget(minSupportedJavaVersion)
        freeCompilerArgs.addAll(
            "-Xjdk-release=${minSupportedJavaVersion.majorVersion}",
        )
    }

    testRuns.configureEach {
        executionTask.configure {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion(minSupportedJavaVersion)
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = minSupportedJavaVersion.toString()
    targetCompatibility = minSupportedJavaVersion.toString()
}
//endregion


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
