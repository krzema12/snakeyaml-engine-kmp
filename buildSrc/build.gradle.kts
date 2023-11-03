plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:5.8.0")
    //endregion

    implementation("org.jetbrains:annotations:24.0.1")
}
