package buildsrc.conventions.lang

import buildsrc.utils.JavaLanguageVersion
import buildsrc.utils.JvmTarget
import buildsrc.utils.compilerFor
import buildsrc.utils.launcherFor
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Base configuration for all Kotlin/Multiplatform conventions.
 *
 * This plugin does not enable any Kotlin target. To enable a target in a subproject, prefer applying specific Kotlin
 * target convention plugins.
 */

plugins {
    id("buildsrc.conventions.base")
    kotlin("multiplatform")
    id("io.kotest")
    id("com.google.devtools.ksp")
}


@OptIn(
    ExperimentalKotlinGradlePluginApi::class,
    ExperimentalWasmDsl::class,
)
kotlin {
    // Use modern JDK to build the project.
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
val minSupportedJavaVersion = JavaVersion.VERSION_1_8

val javaForTests = JavaVersion.VERSION_11

kotlin.targets.withType<KotlinJvmTarget>().configureEach {
    // Compiling Kotlin production code.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        jvmTarget = JvmTarget(minSupportedJavaVersion)
        freeCompilerArgs.addAll(
            "-Xjdk-release=${minSupportedJavaVersion.majorVersion}",
        )
    }

    // Running tests.
    testRuns.configureEach {
        executionTask.configure {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion(javaForTests)
            }
        }
    }
}

// Compiling Kotlin tests.
tasks.named<KotlinJvmCompile>("compileTestKotlinJvm") {
    compilerOptions {
        jvmTarget = JvmTarget(javaForTests)
    }
}

// Compiling Java production code.
tasks.named<JavaCompile>("compileJava") {
    sourceCompatibility = minSupportedJavaVersion.toString()
    targetCompatibility = minSupportedJavaVersion.toString()
}

// Compiling Java tests.
tasks.named<JavaCompile>("compileTestJava") {
    sourceCompatibility = javaForTests.toString()
    targetCompatibility = javaForTests.toString()
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
