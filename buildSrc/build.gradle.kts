plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:5.9.0")
    //endregion

    implementation("org.jetbrains:annotations:24.1.0")

    implementation("io.github.java-diff-utils:java-diff-utils:4.12")
}
