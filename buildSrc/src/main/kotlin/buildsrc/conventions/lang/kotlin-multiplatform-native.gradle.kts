package buildsrc.conventions.lang

/** conventions for a Kotlin/Native subproject */

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
}

kotlin {
    linuxX64()

    mingwX64()

    macosX64()
    macosArm64()

    ios()     // iosArm64, iosX64
    watchos() // watchosArm32, watchosArm64, watchosX64
    tvos()    // tvosArm64, tvosX64

    iosSimulatorArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
}
