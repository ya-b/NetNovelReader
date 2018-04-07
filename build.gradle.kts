buildscript {
    repositories {
        maven("https://dl.bintray.com/kotlin/ktor")
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(deps.buildPlugins.android)
        classpath(deps.buildPlugins.kotlin)
        classpath(deps.buildPlugins.tomcat)
    }
}

allprojects {
    repositories {
        maven("https://dl.bintray.com/kotlin/ktor")
        jcenter()
        google()
        mavenCentral()
    }
}

task("clean", Delete::class) {
    delete { rootProject.buildDir }
}