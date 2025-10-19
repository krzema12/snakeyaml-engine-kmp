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

        jvmTest {
            dependencies {
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.junit5)

                implementation(libs.kotlin.compile.testing.ksp)
                implementation(libs.turtle)
            }
        }
    }
}
