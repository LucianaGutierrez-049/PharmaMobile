import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Credenciales de firma fuera de Git (ver README). Sin el archivo, debug sigue compilando.
val firma = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val tieneFirma = firma.getProperty("storeFile") != null

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Sin configuración real la app funciona localmente y la telemetría queda inactiva.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation(libs.kotlinx.coroutines.core)
    androidTestImplementation(libs.ktor.client.core)
    androidTestImplementation(libs.ktor.client.auth)
}

android {
    namespace = "pe.edu.upeu.pharmamobilee"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobilee"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "pe.edu.upeu.pharmamobilee.Integrador13Instrumentation"
        // Vacía por defecto: el emulador usa 10.0.2.2. Para un teléfono: -Ppharma.apiUrl=http://localhost:8080/api/v1/
        resValue("string", "pharma_api_url", providers.gradleProperty("pharma.apiUrl").getOrElse(""))
    }
    signingConfigs {
        if (tieneFirma) {
            create("release") {
                storeFile = file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            if (tieneFirma) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        resValues = true
    }
}
