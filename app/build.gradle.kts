plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.elad.amonguscompanion"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.elad.amonguscompanion"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
