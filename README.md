# Harmony Music Player

Harmony is a music player for Android that allows you to play music from various sources.

## Project Structure

The project is a standard Android application with a few key directories:

*   **`app`**: This directory contains the main application module, including the source code, resources, and build files.
*   **`NewPipeExtractor`**: This is a submodule that contains the NewPipe Extractor, which is used to extract video information from various streaming services.
*   **`buildSrc`**: This directory contains custom build logic for the project.
*   **`gradle`**: This directory contains the Gradle wrapper, which ensures that the correct version of Gradle is used for the build.
*   **`fastlane`**: This directory contains files for automating the release process.
*   **`checkstyle`**: This directory contains the Checkstyle configuration files, which are used to enforce a consistent coding style.

## Build Process

The project is built using Gradle and requires a local Android SDK installation.

The project uses the Gradle Java toolchain and requires Java 25 for local builds.

1. Copy the example file and set your SDK path:

```bash
cp local.properties.example local.properties
```

Then edit `local.properties` and set `sdk.dir` to your Android SDK directory. If `local.properties` is missing, the build now auto-detects `ANDROID_HOME`/`ANDROID_SDK_ROOT` and generates it for you.

Run the environment preflight check:

```bash
./scripts/verify-android-env.sh
```

2. Build a debug APK:

Make sure your Android SDK includes:

* Android SDK Platform 35
* Android SDK Build-Tools 35.0.0

```bash
./gradlew assembleDebug
```

If Platform 36 is not installed locally yet, use:

```bash
./gradlew assembleDebug -Pandroid.compileSdk=35 -Pandroid.targetSdk=35
```

This creates a debug APK in `app/build/outputs/apk/debug`.

3. Build a release APK:

```bash
./gradlew assembleRelease
```

This creates a release APK in `app/build/outputs/apk/release`.

## Workflow

The application is a music player that allows users to play music from various sources. The main workflow is as follows:

1.  The user opens the application and is presented with a list of their favorite songs.
2.  The user can search for new songs, browse playlists, and view their listening history.
3.  When the user selects a song, it is added to the play queue and begins playing.
4.  The user can control playback using the player controls, which include play, pause, skip, and shuffle.
5.  The application also supports background playback, so the user can continue listening to music while using other applications.

## Firebase Integration

I have added Firebase integration to the project to enable playlist synchronization. The `FirebasePlaylistManager` class is responsible for syncing playlists between the local database and Firebase.

Here's how the Firebase integration works:

1.  When the user logs in, the `FirebasePlaylistManager` is created.
2.  The `FirebasePlaylistManager` subscribes to the `localPlaylistManager.playlists` `Flowable` to get a list of all local playlists.
3.  The `FirebasePlaylistManager` then pushes the local playlists to Firebase.
4.  The `FirebasePlaylistManager` also fetches a list of remote playlists from Firebase and updates the local database.
