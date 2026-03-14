# Build Process

This document outlines the process for building the Harmony Music Player APK.

## 1. Configure Java Toolchain

The project requires Java 25 (as configured by the Gradle toolchain in the app module). To ensure the correct JDK is used, the `foojay-resolver-convention` plugin is used to automatically download the correct JDK. This is configured in the `settings.gradle.kts` file:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}
```

## 2. Configure Android SDK Path

The Android Gradle plugin also requires a configured Android SDK path.

1. Copy the template:

```bash
cp local.properties.example local.properties
```

2. Edit `local.properties` and set:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Without this file (or a valid `ANDROID_HOME`/`ANDROID_SDK_ROOT`), Gradle fails with `SDK location not found`.

Run the environment preflight check:

```bash
./scripts/verify-android-env.sh
```

## 3. Install required Android SDK packages

Install these SDK components in your Android SDK manager:

- Android SDK Platform 36 (default project compile SDK)
- Android SDK Platform 35 (optional local fallback)
- Android SDK Build-Tools 35.0.0
- Android SDK Command-line Tools (latest)

## 4. Build the APK

Once Java and SDK are configured, build the APK using:

```bash
./gradlew assembleDebug
```

This will create a debug APK in the `app/build/outputs/apk/debug` directory.

If your local machine does not have Platform 36 yet, you can build with the fallback:

```bash
./gradlew assembleDebug -Pandroid.compileSdk=35 -Pandroid.targetSdk=35
```

## 5. Build APK in GitHub Actions

A GitHub Actions workflow is available at `.github/workflows/build-release-apk.yml`.

It runs on every push, pull request, and manual trigger (`workflow_dispatch`) to:

- Build debug APK (`assembleDebug`)
- Build release APK (`assembleRelease`)
- Upload both APK files as workflow artifacts

You can download generated APK files from the **Artifacts** section of the workflow run in GitHub.
