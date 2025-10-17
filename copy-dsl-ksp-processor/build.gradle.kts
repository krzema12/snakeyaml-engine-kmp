plugins {
    buildsrc.conventions.lang.`kotlin-multiplatform`
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.ksp.symbol.processing.api)
                implementation(libs.kotlinpoet)
                implementation(libs.kotlinpoet.ksp)
            }
        }
    }
}
