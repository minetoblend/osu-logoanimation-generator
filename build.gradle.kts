plugins {
    kotlin("multiplatform") version "2.2.21"
}

group = "com.hautzy.connecdoor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    mingwX64 {
        binaries.executable()
    }
    macosX64 {
        binaries.executable()
    }
    macosArm64 {
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.clikt)
            implementation("com.soywiz.korlibs.korim:korim:4.0.10")
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
            implementation(libs.mordant)
            implementation(libs.mordant.coroutines)
        }
    }
}
