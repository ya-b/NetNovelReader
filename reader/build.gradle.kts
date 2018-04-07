import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(config.compileSdk)

    defaultConfig {
        minSdkVersion(config.minSdk)
        targetSdkVersion(config.targetSdk)
        applicationId = config.applicationId
        versionCode = config.versionCode
        versionName = config.versionName
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    dataBinding {
        setEnabled(true)
    }
    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }
    kapt {
        useBuildCache = true
        mapDiagnosticLocations = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas".toString())
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":pageview"))
    implementation(project(":flowlayout"))
    implementation(deps.android.support.v4)
    implementation(deps.android.support.v7)
    implementation(deps.android.support.cardview)
    implementation(deps.android.support.design)
    implementation(deps.android.support.constraint_layout)
    implementation(deps.android.support.preference)
    kapt(deps.android.databinding)
    implementation(deps.circleimageview)
    implementation(deps.android.arch.lifecycle)
    kapt(deps.android.arch.lifecycle_compiler)
    testImplementation(deps.android.arch.lifecycle_test)
    implementation(deps.android.arch.room)
    kapt(deps.android.arch.room_compiler)
    testImplementation(deps.android.arch.room_test)
    implementation(deps.kotlin.stdlib.jdk)
    implementation(deps.kotlin.coroutines.core)
    implementation(deps.kotlin.coroutines.android)
    implementation(deps.jsoup)
    implementation(deps.retrofit.client)
    implementation(deps.retrofit.converter_scalars)
    implementation(deps.retrofit.converter_gson)
    debugImplementation(deps.leakcanary.debug)
    releaseImplementation(deps.leakcanary.release)
    testImplementation(deps.test.junit)
    testImplementation(deps.test.mockito)
    testImplementation(deps.test.hamcrest)
    androidTestImplementation(deps.test.runner)
    androidTestImplementation(deps.test.rules)
    androidTestImplementation(deps.test.junit)
    androidTestImplementation(deps.test.mockito_android)
    androidTestImplementation(deps.test.espresso.core)
    androidTestImplementation(deps.test.espresso.contrib)
    androidTestImplementation(deps.test.espresso.intents)
    androidTestImplementation(deps.test.espresso.idling_concurrent)
    androidTestImplementation(deps.test.espresso.idling_resource)
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}
