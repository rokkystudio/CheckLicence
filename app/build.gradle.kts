plugins {
    id("com.android.library")
    id("maven-publish")
}

group = "com.rokkystudio.checklicence"
version = "0.1"

android {
    namespace = "com.rokkystudio.checklicence"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildToolsVersion = "34.0.0"

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("org.apache.httpcomponents:httpcore:4.4.16")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}