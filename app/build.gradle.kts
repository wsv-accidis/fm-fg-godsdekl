import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "se.accidis.fmfg.app"

	defaultConfig {
		applicationId = "se.accidis.fmfg.app"
		compileSdk = 35
		minSdk = 26
		targetSdk = 35
		versionCode = 18
		versionName = "1.8"
		resourceConfigurations += listOf("en", "sv")
	}

	buildFeatures {
		buildConfig = true
	}

	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	aaptOptions {
		ignoreAssetsPattern = "!adr-s.json:!amkat.json"
	}
}

dependencies {
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	implementation("androidx.appcompat:appcompat:1.7.1")
	implementation("androidx.preference:preference:1.2.1")
	implementation("com.google.android.material:material:1.13.0")
	implementation("net.danlew:android.joda:2.14.0")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.fromTarget("17"))
	}
}
