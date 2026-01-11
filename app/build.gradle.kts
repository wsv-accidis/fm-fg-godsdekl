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
	}


	androidResources {
		ignoreAssetsPatterns += listOf("!adr-s.json", "!amkat.json")
		localeFilters += listOf("en", "sv")
	}

	buildFeatures {
		buildConfig = true
	}

	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			setProguardFiles(
				listOf(
					getDefaultProguardFile("proguard-android-optimize.txt"),
					"proguard-rules.pro"
				)
			)
		}
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
