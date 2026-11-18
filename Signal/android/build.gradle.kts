plugins {
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.kotlin.android") version "2.1.21"
}

android {
    namespace = "games.signal.collection"
    compileSdk = 35
    if (providers.gradleProperty("lightArt").orNull == "true") {
        sourceSets.getByName("main").assets.setSrcDirs(listOf(layout.buildDirectory.dir("light-assets")))
        for (variant in listOf("debug", "release")) {
            sourceSets.getByName(variant).manifest.srcFile("src/light/AndroidManifest.xml")
        }
    } else if (providers.gradleProperty("compactArt").orNull == "true") {
        sourceSets.getByName("main").assets.setSrcDirs(listOf(layout.buildDirectory.dir("compact-assets")))
    }
    defaultConfig {
        applicationId = "games.signal.collection"
        minSdk = 23
        targetSdk = 32
        versionCode = 20
        versionName = "2.4.0"
    }
}
