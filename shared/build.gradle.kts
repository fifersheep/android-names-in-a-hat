plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "uk.lobsterdoodle.namepicker.shared"
        compileSdk = 37
        minSdk = 26
        withHostTest {}
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // commonMain stays free of Android-only and DI libraries: Hilt lives in :app only.
        commonMain.dependencies {
            // api: the public repository interfaces expose Flow.
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
