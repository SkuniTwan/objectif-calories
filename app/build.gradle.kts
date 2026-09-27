import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// URL du relais Cloudflare Worker (voir worker/README.md) : jamais de clé API Mistral
// dans l'app elle-même, seulement l'adresse du relais + un secret partagé app↔Worker
// (extractible par décompilation comme tout ce qui est embarqué côté client, mais
// n'expose jamais la clé Mistral elle-même — voir CLAUDE.md). Lus depuis
// local.properties (git-ignoré), vides par défaut pour que le projet compile chez qui
// n'a pas encore déployé son Worker (MistralAnalyzer échoue alors explicitement à
// l'exécution plutôt que de planter au build).
val localProperties = Properties().apply {
    val fichier = rootProject.file("local.properties")
    if (fichier.exists()) fichier.inputStream().use { load(it) }
}

android {
    namespace = "com.legoueix.objectifcalories"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.legoueix.objectifcalories"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField(
            "String",
            "WORKER_URL",
            "\"${localProperties.getProperty("WORKER_URL", "")}\"",
        )
        buildConfigField(
            "String",
            "APP_SHARED_SECRET",
            "\"${localProperties.getProperty("APP_SHARED_SECRET", "")}\"",
        )
    }

    // Signature de release : lue depuis local.properties (git-ignoré), jamais en dur.
    // Absente chez qui n'a pas encore généré/renseigné son keystore — un ./gradlew
    // assembleRelease échoue alors explicitement plutôt que de produire un .aab non
    // signable, au lieu de silencieusement retomber sur la signature debug.
    val cheminKeystore = localProperties.getProperty("RELEASE_STORE_FILE")
    signingConfigs {
        if (cheminKeystore != null) {
            create("release") {
                storeFile = rootProject.file(cheminKeystore)
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (cheminKeystore != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.exifinterface)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.okhttp)
}
