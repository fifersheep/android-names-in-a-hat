// AGP 9 compiles Kotlin itself (built-in Kotlin): do not apply org.jetbrains.kotlin.android here.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    // Navigation 3 keys are @Serializable so the back stack survives process death.
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing is configured only when all four niah* properties are supplied
// (niahKeystoreFile in gradle.properties, the secrets in ~/.gradle/gradle.properties),
// so debug builds and CI work without the keystore.
val releaseSigning: Map<String, String>? =
    listOf("niahKeyAlias", "niahKeyPassword", "niahKeystoreFile", "niahKeystorePassword")
        .associateWith { providers.gradleProperty(it).orNull.orEmpty() }
        .takeIf { props -> props.values.all { it.isNotBlank() } }

android {
    namespace = "uk.lobsterdoodle.namepicker"
    // 37 because the current Compose BOM, core-ktx and androidx.hilt declare minCompileSdk 37.
    compileSdk = 37

    defaultConfig {
        // Must never change: this is the Play listing.
        applicationId = "uk.lobsterdoodle.namepicker"
        minSdk = 26
        targetSdk = 36
        // 2000000000 is burned and 2000000001 is 2.7.0 on Play; +1 per release (Play max 2100000000).
        versionCode = 2000000002
        versionName = "3.0.0"
    }

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                // The path is relative to the repo root; the keystore sits beside the repo.
                storeFile = rootProject.file(releaseSigning.getValue("niahKeystoreFile"))
                storePassword = releaseSigning.getValue("niahKeystorePassword")
                keyAlias = releaseSigning.getValue("niahKeyAlias")
                keyPassword = releaseSigning.getValue("niahKeyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseSigning != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        // ViewModels log with android.util.Log; JVM unit tests get no-op stubs instead of throwing.
        unitTests.isReturnDefaultValues = true
    }

    lint {
        // Unavoidable: the Play listing already sits at 2000000001, so every future code is "very high".
        disable += "HighAppVersionCode"
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.compose.material3.adaptive.layout)
    implementation(libs.compose.material3.adaptive.navigation3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // AGP 9 built-in Kotlin doesn't infer kotlin-test's JUnit adapter, so it is named explicitly.
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
