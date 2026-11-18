plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.1.21"
}

android {
    namespace = "games.signal.collection"
    compileSdk = 35
    defaultConfig {
        applicationId = "games.signal.collection"
        minSdk = 23
        targetSdk = 32
        versionCode = 13
        versionName = "1.7.1"
    }
}
