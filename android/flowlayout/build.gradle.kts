plugins {
    id("com.android.library")
}

android {
    compileSdkVersion(config.compileSdk)
    defaultConfig {
        minSdkVersion(config.minSdk)
        targetSdkVersion(config.targetSdk)
        versionCode = config.versionCode
        versionName = config.versionName
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}