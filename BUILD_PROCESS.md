# Build Process

This document outlines the process for building the Harmony Music Player APK.

## 1. Configure Java Toolchain

The project requires Java 17. To ensure the correct JDK is used, the `foojay-resolver-convention` plugin is used to automatically download the correct JDK. This is configured in the `settings.gradle.kts` file:

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

## 2. Build the APK

Once the Java toolchain is configured, you can build the APK using the following command:

```bash
./gradlew assembleDebug
```

This will create a debug APK in the `app/build/outputs/apk/debug` directory.

## 3. Build APK in GitHub Actions

A GitHub Actions workflow is available at `.github/workflows/build-release-apk.yml`.

It runs on every push, pull request, and manual trigger (`workflow_dispatch`) to:

- Build debug APK (`assembleDebug`)
- Build release APK (`assembleRelease`)
- Upload both APK files as workflow artifacts

You can download generated APK files from the **Artifacts** section of the workflow run in GitHub.
