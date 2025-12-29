plugins {
    alias(libs.plugins.kotlinMultiplatform)
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
    linuxX64 {
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.clikt)
            implementation(libs.korlibs.korim)
            implementation(libs.mordant)
            implementation(libs.mordant.coroutines)
        }
    }
}
