import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.petnotifications"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.petnotifications"
        // TYPE_APPLICATION_OVERLAY (mascota flotante) requiere API 26+
        minSdk = 26
        targetSdk = 36
        // Subir appVersionCode (gradle.properties) en cada APK que se entrega: con el mismo número,
        // algunos celulares (Xiaomi) no ofrecen actualizar y abren la app ya instalada.
        val build = (property("appVersionCode") as String).toInt()
        versionCode = build
        versionName = "1.0.$build"
    }

    // Dos versiones de la app, iguales salvo la duración de la prueba gratis de Pro:
    //  - closedTest: la de la prueba cerrada de Google Play (7 días, para que los testers vean el final).
    //  - production: la que se publica (14 días).
    // En la Play Console, a producción se sube la compilación "production": NO promover la de la prueba
    // cerrada, porque se quedaría con 7 días.
    // Herramientas de dueño (forzar Pro o plan gratis, corazones del vínculo): solo en las versiones de
    // prueba que se arman con -PownerTools=true. Nunca en lo que se sube a Google Play (ver la
    // verificación de abajo, que frena el paquete para Play si viene con esto activado).
    val ownerTools = (project.findProperty("ownerTools") as String?) == "true"
    defaultConfig.buildConfigField("boolean", "OWNER_TOOLS", ownerTools.toString())
    if (ownerTools && gradle.startParameter.taskNames.any { it.contains("bundle", ignoreCase = true) }) {
        throw GradleException("El paquete para Google Play no puede llevar las herramientas de dueño (-PownerTools=true).")
    }

    flavorDimensions += "channel"
    productFlavors {
        create("closedTest") {
            dimension = "channel"
            buildConfigField("int", "TRIAL_DAYS", "7")
        }
        create("production") {
            dimension = "channel"
            buildConfigField("int", "TRIAL_DAYS", "7")
        }
    }

    // Llave para subir a Google Play: se lee de keystore.properties (fuera del repositorio). Sin ese
    // archivo, la versión final se firma con la llave de prueba: sirve para probarla en el celular,
    // pero Google Play la rechaza, así que no se puede publicar por error.
    val keystoreFile = rootProject.file("keystore.properties")
    signingConfigs {
        if (keystoreFile.exists()) {
            val props = Properties().apply { keystoreFile.inputStream().use { load(it) } }
            create("upload") {
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8: achica la app y dificulta copiar el código (problema L de la revisión de Cowork).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        aidl = false
        buildConfig = true
        shaders = false
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.billing.ktx)

    testImplementation(libs.junit)
}

// La app promete que no tiene acceso a internet ("tus datos nunca llegan a nosotros"). Si alguna librería agrega permisos de red al
// manifiesto final, la compilación de release falla: hay que revisar qué librería fue antes de
// quitarlos con tools:node="remove". Se revisa cada versión (closedTest y production).
listOf("ClosedTest", "Production").forEach { flavor ->
    val variant = "${flavor.replaceFirstChar { it.lowercase() }}Release"
    val verify = tasks.register("verifyNoNetworkPermissions$flavor") {
        val manifest = layout.buildDirectory.file(
            "intermediates/merged_manifests/$variant/process${flavor}ReleaseManifest/AndroidManifest.xml",
        )
        dependsOn("process${flavor}ReleaseManifest")
        inputs.file(manifest)
        doLast {
            val text = manifest.get().asFile.readText()
            val found = listOf("android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE")
                .filter { it in text }
            if (found.isNotEmpty()) {
                throw GradleException(
                    "El manifiesto de $variant pide $found. Ver manifest-merger-$variant-report.txt para saber qué librería los agrega.",
                )
            }
        }
    }
    tasks.matching { it.name == "assemble${flavor}Release" || it.name == "bundle${flavor}Release" }.configureEach {
        dependsOn(verify)
    }
}
