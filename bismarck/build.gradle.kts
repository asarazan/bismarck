plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinKover)
    alias(libs.plugins.mavenPublish)
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

kotlin {
    jvm()
    js(IR) {
        nodejs()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.stately.concurrency)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.kotlinx.nodejs)
        }
    }
}
