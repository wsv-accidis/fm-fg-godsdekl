plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "se.accidis.fmfg.app"

	defaultConfig {
		applicationId = "se.accidis.fmfg.app"
		compileSdk = 36
		minSdk = 26
		targetSdk = 36
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
	implementation(libs.androidx.appcompat) // possibly legacy
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.navigation3.runtime)
	implementation(libs.androidx.navigation3.ui)
	implementation(libs.androidx.preference)
	implementation(libs.danlew.joda)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.google.material) // legacy
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}
