plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:6.0.0.M1")
    //endregion

    implementation("org.jetbrains:annotations:26.0.1")

    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
}
