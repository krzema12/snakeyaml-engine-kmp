plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
    implementation(libs.kotest.framework.multiplatform.plugin.gradle)
    //endregion

    implementation("org.jetbrains:annotations:26.0.2")

    implementation("io.github.java-diff-utils:java-diff-utils:4.16")
}
