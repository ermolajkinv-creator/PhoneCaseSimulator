plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.1.21"
}

android {
    namespace = "games.signal.collection"
    compileSdk = 35
    if (providers.gradleProperty("compactArt").orNull == "true") {
        sourceSets.getByName("main").assets.setSrcDirs(listOf(layout.buildDirectory.dir("compact-assets")))
    }
    defaultConfig {
        applicationId = "games.signal.collection"
        minSdk = 23
        targetSdk = 32
        versionCode = 18
        versionName = "2.2.0"
    }
}
