package buildsrc.conventions.lang

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

relocateKotlinJsStore()
