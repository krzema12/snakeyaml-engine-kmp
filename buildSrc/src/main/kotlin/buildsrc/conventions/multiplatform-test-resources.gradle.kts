package buildsrc.conventions

/**
 * Make test resources from the common source set available on all targets
 * by putting them in a Kotlin class that can be later compiled.
 */

plugins {
    id("buildsrc.conventions.base")
}

val convertCommonTestResourcesToKotlin by tasks.registering(buildsrc.tasks.ConvertCommonTestResourcesToKotlin::class) {
    description = "Generate Kotlin code for accessing the common test resources"
    group = "verification"

    destination.set(layout.buildDirectory.dir("generated-code-for-resources/src/commonTest/kotlin"))
}
