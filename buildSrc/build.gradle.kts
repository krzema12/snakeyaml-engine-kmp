plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:5.9.0")
    //endregion

    implementation("org.jetbrains:annotations:24.1.0")
}
