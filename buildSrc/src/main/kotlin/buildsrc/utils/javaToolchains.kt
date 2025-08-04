package buildsrc.utils

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Utilities for configuring Java Toolchains

internal fun JavaToolchainService.compilerFor(version: Int): Provider<JavaCompiler> =
    compilerFor { languageVersion = JavaLanguageVersion.of(version) }

internal fun JavaToolchainService.launcherFor(version: JavaVersion): Provider<JavaLauncher> =
    launcherFor { languageVersion = JavaLanguageVersion.of(version.majorVersion) }

internal fun JvmTarget(version: JavaVersion): JvmTarget =
    JvmTarget.fromTarget(version.toString())

internal fun JavaLanguageVersion(version: JavaVersion): JavaLanguageVersion =
    JavaLanguageVersion.of(version.majorVersion)
