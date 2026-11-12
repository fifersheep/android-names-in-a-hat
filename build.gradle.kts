// Every plugin is declared here (apply false) so all modules share one classloader.
// Declaring kotlin-multiplatform at the root also lifts the Kotlin Gradle Plugin that
// AGP 9's built-in Kotlin uses in :app from AGP's bundled default up to libs.versions.kotlin.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.androidx.room3) apply false
}
