/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */


import java.util.Properties

val localPropertiesFile = file("local.properties")
if (!localPropertiesFile.exists()) {
    val sdkFromEnv = sequenceOf("ANDROID_HOME", "ANDROID_SDK_ROOT")
        .mapNotNull { System.getenv(it) }
        .firstOrNull { it.isNotBlank() }

    if (sdkFromEnv != null) {
        val generatedProperties = Properties().apply {
            setProperty("sdk.dir", sdkFromEnv)
        }

        localPropertiesFile.outputStream().use { generatedProperties.store(it, null) }
        println("Generated local.properties from ANDROID_HOME/ANDROID_SDK_ROOT for this build.")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
                "org.jetbrains.kotlin.android",
                "org.jetbrains.kotlin.kapt",
                "org.jetbrains.kotlin.plugin.parcelize" -> {
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                }
                "com.google.devtools.ksp" -> {
                    useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
                }
            }
        }
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repo.clojars.org")
    }
}
include (":app")
