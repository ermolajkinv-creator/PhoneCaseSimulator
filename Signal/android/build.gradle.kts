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
    } else {
        sourceSets.getByName("main").assets.setSrcDirs(listOf(layout.buildDirectory.dir("offline-assets")))
        val prepareOfflineAssets = tasks.register<Exec>("prepareOfflineAssets") {
            val script = rootProject.file("tools/build-offline.py")
            inputs.file(script)
            inputs.dir("src/main/assets")
            outputs.dir(layout.buildDirectory.dir("offline-assets"))
            commandLine("python3", script.absolutePath)
        }
        tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(prepareOfflineAssets) }
    }
    androidResources.noCompress += "pack"
    defaultConfig {
        applicationId = "games.signal.collection"
        minSdk = 23
        targetSdk = 32
        versionCode = 22
        versionName = "2.5.1"
    }
}
