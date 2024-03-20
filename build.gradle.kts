plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinKover) apply false
}

val GROUP: String by project
val VERSION_NAME: String by project

allprojects {
    group = GROUP
    version = VERSION_NAME
}

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()
    }
}
