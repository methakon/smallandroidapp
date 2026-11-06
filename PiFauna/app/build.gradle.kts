import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/*
 * Upload signing material is read from outside the repository. Point
 * LAYASWARNA_KEYSTORE_PROPERTIES at a properties file, or leave it unset to use
 * ~/.config/layaswarna/keystore.properties. Nothing here is ever committed, and
 * a release build without that file stays unsigned rather than failing loudly in
 * CI, which only builds debug variants.
 */
val keystorePropertiesFile: File =
    providers.environmentVariable("LAYASWARNA_KEYSTORE_PROPERTIES").orNull
        ?.let { file(it) }
        ?: File(System.getProperty("user.home"), ".config/layaswarna/keystore.properties")

val keystoreProperties: Properties? =
    if (keystorePropertiesFile.isFile) {
        Properties().apply { keystorePropertiesFile.inputStream().use { load(it) } }
    } else {
        logger.lifecycle(
            "No signing properties at $keystorePropertiesFile — release output will be unsigned."
        )
        null
    }

android {
    namespace = "in.berhampore.dhar.layaswarna"
    compileSdk = 36

    defaultConfig {
        applicationId = "in.berhampore.dhar.layaswarna"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "0.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            // Play warns when an app ships native libraries without symbols.
            // This app has no C/C++ of its own; the only .so comes from an AndroidX
            // dependency, so a symbol table is enough to resolve the warning.
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        // Safety-critical findings must be visible in the report, but a warning
        // about, say, an unused resource should not fail the build.
        warningsAsErrors = false
        abortOnError = true
        checkDependencies = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
