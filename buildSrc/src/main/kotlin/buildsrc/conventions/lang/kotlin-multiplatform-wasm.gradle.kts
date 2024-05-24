package buildsrc.conventions.lang

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    id("buildsrc.conventions.lang.kotlin-multiplatform-base")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    wasmJs {
        nodejs()
    }

    wasmWasi {
        nodejs()
    }
}
