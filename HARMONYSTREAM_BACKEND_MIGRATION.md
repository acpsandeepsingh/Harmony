# HarmonyStream Backend Migration Guide

## 1) Remove NewPipe extractor wiring from app-level backend
- Delete `NewPipeStreamExtractor` and `FirebaseVideoService`.
- Update `HomeFragment` so playback requests stream URLs from `VideoRepository`.
- Update `PlayerManager` to consume plain stream URLs.

## 2) Add HarmonyStream API layer
Create:
- `app/src/main/java/com/sansoft/harmony/api/ApiService.java`
- `app/src/main/java/com/sansoft/harmony/api/RetrofitInstance.java`
- `app/src/main/java/com/sansoft/harmony/api/ApiClient.java`

Endpoints used:
- `GET /search?q={query}`
- `GET /videos/{videoId}`
- `GET /videos/{videoId}/stream`

## 3) Add network models
Create:
- `app/src/main/java/com/sansoft/harmony/models/Video.java`
- `app/src/main/java/com/sansoft/harmony/models/SearchResponse.java`
- `app/src/main/java/com/sansoft/harmony/models/StreamResponse.java`

## 4) Update repository
- `VideoRepository` now uses `ApiClient` for:
  - search
  - fetch details
  - fetch stream URL
- Existing Room caching behavior is kept (`VideoDao`/`VideoEntity`) and maps into existing `VideoItem` UI model.

## 5) Player integration
- Add `ExoPlayerManager` with methods:
  - `play`
  - `pause`
  - `seekTo`
  - `setFullscreen`
  - `release`

## 6) Gradle dependency updates
- Remove:
  - `libs.newpipe.nanojson`
  - `libs.newpipe.extractor`
  - `libs.newpipe.filepicker`
- Add:
  - Retrofit core + Gson converter
  - Gson

## 7) Validation
- Run Gradle compile task:
  - `./gradlew :app:compileDebugJavaWithJavac`
- If SDK is not configured, add `sdk.dir=` to `local.properties` or set `ANDROID_HOME`, then rerun.
