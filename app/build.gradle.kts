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
	implementation(files("libs/PDFjet.jar"))
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.preference)
	implementation(libs.google.material)
	implementation(libs.danlew.joda)
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
