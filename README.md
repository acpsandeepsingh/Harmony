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

The project is built using Gradle. To build the project, you can use the following command:

```bash
./gradlew assembleDebug
```

This will create a debug APK in the `app/build/outputs/apk/debug` directory.

To build a release APK, you can use the following command:

```bash
./gradlew assembleRelease
```

This will create a release APK in the `app/build/outputs/apk/release` directory.

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
