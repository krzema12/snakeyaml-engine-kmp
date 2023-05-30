import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
}

//val buildLogicJvmTarget = "11"
//
//
//kotlin {
//   jvmToolchain {
//      (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(buildLogicJvmTarget))
//   }
//}
//
//tasks.withType<KotlinCompile>().configureEach {
//   kotlinOptions {
//      jvmTarget = buildLogicJvmTarget
//   }
//}
//
//kotlinDslPluginOptions {
//   jvmTarget.set(buildLogicJvmTarget)
//}
