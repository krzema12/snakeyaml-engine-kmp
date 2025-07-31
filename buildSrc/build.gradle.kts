plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotest.framework.multiplatform.plugin.gradle)
    implementation(libs.maven.publish.gradle.plugin)
    //endregion

    implementation(libs.jetbrains.annotations)

    implementation(libs.java.diff.utils)
}
