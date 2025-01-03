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

    // HACK: Ideally we'd retrieve the 'resources' path via Gradle API, but somehow Gradle sees only 'main' and 'test'
    // source sets instead of the required 'commonTest'.
    commonResourcesDir.set(project.projectDir.resolve("src").resolve("commonTest").resolve("resources"))
    destination.set(layout.buildDirectory.dir("generated-code-for-resources/src/commonTest/kotlin"))
}
