buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // AGP 9 内置 Kotlin（built-in Kotlin）：用 classpath 指定更高的 KGP 版本
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.application") version "9.5.0" apply false
}
