import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.21"
}

// 登录态从 local.properties 注入（不随代码提交），留空即游客模式
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val feiyanToken: String = localProperties.getProperty("feiyan.token").orEmpty().trim()
val bookyellToken: String = localProperties.getProperty("bookyell.token").orEmpty().trim()

// 自更新仓库：owner/repo（本地写 local.properties，CI 用环境变量注入）
val updateRepo: String = (System.getenv("UPDATE_REPO")
    ?: localProperties.getProperty("update.repo")).orEmpty().trim()

// 版本号单一来源：gradle.properties 的 app.versionName / app.versionCode，
// CI 发布时仍可用 VERSION_NAME / VERSION_CODE 环境变量覆盖
val appVersionName: String =
    (findProperty("app.versionName") as? String)?.trim().orEmpty().ifBlank { "1.0.3" }
val appVersionCode: Int =
    (findProperty("app.versionCode") as? String)?.trim()?.toIntOrNull() ?: 1

// 发布签名：优先环境变量（CI），其次 keystore.properties；都没有则回落 debug 签名
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun envOrProp(key: String, prop: String): String? =
    System.getenv(key) ?: keystoreProps.getProperty(prop)

android {
    namespace = "com.feiyan.english"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.feiyan.english"
        minSdk = 26
        targetSdk = 37
        versionCode = System.getenv("VERSION_CODE")?.trim()?.toIntOrNull() ?: appVersionCode
        versionName = System.getenv("VERSION_NAME") ?: appVersionName
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "FEIYAN_TOKEN", "\"$feiyanToken\"")
        buildConfigField("String", "BOOKYELL_TOKEN", "\"$bookyellToken\"")
        buildConfigField("String", "UPDATE_REPO", "\"$updateRepo\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        create("release") {
            val storePath = envOrProp("KEYSTORE_FILE", "storeFile")
            if (!storePath.isNullOrBlank() && rootProject.file(storePath).exists()) {
                storeFile = rootProject.file(storePath)
                storePassword = envOrProp("KEYSTORE_PASSWORD", "storePassword")
                keyAlias = envOrProp("KEY_ALIAS", "keyAlias")
                keyPassword = envOrProp("KEY_PASSWORD", "keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            // R8 full mode + 资源裁剪；签名优先用发布证书，缺省回落 debug（便于侧载自更新）
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
        )
    }
}

kotlin {
    compilerOptions {
        // 内置 Kotlin 默认跟随 android.compileOptions，这里显式声明更清晰
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.10.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-session:1.11.1")
    // 让播放器复用带鉴权头的 OkHttp（BookYell 朗读接口需要 Authorization）
    implementation("androidx.media3:media3-datasource-okhttp:1.11.1")

    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.3")

    // Haze：毛玻璃/背景模糊
    implementation("dev.chrisbanes.haze:haze:2.0.1")
    implementation("dev.chrisbanes.haze:haze-blur:2.0.1")
    implementation("dev.chrisbanes.haze:haze-blur-materials:2.0.1")

    implementation("com.squareup.okhttp3:okhttp:5.5.1")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
