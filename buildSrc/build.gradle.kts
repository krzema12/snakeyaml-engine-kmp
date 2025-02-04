plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:6.0.0.M2")
    //endregion

    implementation("org.jetbrains:annotations:26.0.2")

    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
}
