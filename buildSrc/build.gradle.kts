plugins {
    `kotlin-dsl`
}

dependencies {
    //region Gradle Plugins
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:5.7.2")
    //endregion

    implementation("org.jetbrains:annotations:24.0.1")
}
