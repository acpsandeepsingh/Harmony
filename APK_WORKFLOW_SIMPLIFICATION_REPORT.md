# APK Workflow & Simplification Report

## 1) APK workflow check (current state)

### What the project is doing now
- APK is built from the single `:app` module (`settings.gradle.kts` includes only `:app`).
- CI runs `assembleDebug lintDebug testDebugUnitTest -DskipFormatKtlint` and uploads debug APK.
- The app module forces code-format and style checks into `preDebugBuild` (ktlint/checkstyle/dependency-order), which makes the debug build pipeline heavier.

### Will the APK workflow work right now?
**Not fully in current state**.

I ran:

```bash
./gradlew assembleDebug -DskipFormatKtlint --stacktrace
```

Build failed early because Gradle could not resolve:
- `com.android.application` plugin at version `9.0.0`

So the current pipeline is blocked at plugin resolution before actual Android compilation.

---

## 2) Heavy areas in the project (what is making it feel heavy)

### A) Build pipeline heaviness
- `preDebugBuild` is wired to:
  - `runCheckstyle`
  - `runKtlint`
  - `checkDependenciesOrder`
  - and optionally `formatKtlint`
- This means even local debug APK builds perform quality gates.

### B) Dependency surface is broad
The app includes many feature families:
- Extraction/networking (NewPipe extractor, OkHttp, JSoup)
- Playback stack (full ExoPlayer set: dash/hls/smoothstreaming/ui/mediasession)
- Persistence/reactive stack (Room + RxJava + RxAndroid + RxBinding + WorkManager)
- UI/utility (Groupie, Markwon, Coil, Bridge, PrettyTime)
- Debug stack (LeakCanary, Stetho)

This is feature-rich but heavy for a “simple” target.

### C) Documentation drift
`BUILD_PROCESS.md` mentions a workflow file (`build-release-apk.yml`) that does not exist in `.github/workflows` now.

---

## 3) Component classification: needed vs optional vs removable

## Keep (needed for core app behavior)
These are core to a streaming/media app with current codebase:
- `newpipe-extractor`, `newpipe-nanojson`
- ExoPlayer core + currently-used streaming extensions
- `okhttp`, `jsoup`
- `androidx` basics (appcompat/core/fragment/recyclerview/material/preference)
- `room-runtime`, `room-rxjava3`, room compiler (`ksp`)
- `rxjava`, `rxandroid`

## Keep but move out of APK path (build-only / quality tooling)
- `checkstyle`, `ktlint`, `checkDependenciesOrder`
- Recommendation: run these in dedicated CI jobs, not mandatory dependencies of `preDebugBuild`.

## Optional (candidate to disable for “simple mode”)
- `acra` (crash reporting): optional for local/dev builds
- `stetho` (debug bridge): optional
- `leakcanary` debug deps: optional
- `workmanager` features (only if background notification/update sync can be trimmed)
- `markwon` (markdown rendering): optional if rich markdown UI can be simplified
- `bridge` (saved state helper): optional if state logic is refactored to AndroidX ViewModel/SavedState

## High-confidence remove candidate
- `coil-compose` dependency appears unnecessary if you are not using Jetpack Compose UI.
  - I found no `@Composable`, `androidx.compose` imports, or `setContent {}` usage.

## Potentially removable only after feature decision
- ExoPlayer submodules not required by your selected stream protocols:
  - `dash`, `hls`, `smoothstreaming`
- Remove only if you intentionally drop those protocol types.

## Not part of active Gradle build graph right now
- Local `NewPipeExtractor/` directory is present in repo, but only `:app` is included in settings.
  - If this folder is not used for local development, archive/remove it from the repo to reduce project size.

---

## 4) “Use method of old file in new file” migration guidance

To keep behavior while simplifying:

1. **Create a lightweight build path (new)**
   - Keep current full workflow as “strict” (`assembleDebug + lint + tests + style`).
   - Add a lightweight command for quick APK iteration:
   ```bash
   ./gradlew :app:assembleDebug -DskipFormatKtlint
   ```

2. **Preserve old behavior in strict path (old method retained)**
   - Do not delete existing quality tasks.
   - Move them out of `preDebugBuild` and execute in a separate CI job (`quality-checks`) so strict checks still exist.

3. **Feature-flag optional components**
   - Use product flavors/build configs:
     - `full` (current features)
     - `lite` (without optional tools/features)

4. **De-risk dependency removals one-by-one**
   - Remove one candidate
   - Build + smoke test playback/search/download
   - Commit

---

## 5) Recommended phased removal plan

### Phase 0 (unblock build)
- Fix AGP/plugin version compatibility first (currently blocked at plugin resolution).

### Phase 1 (quick wins)
- Remove `coil-compose`.
- Stop gating `preDebugBuild` with style tasks for local APK assembly.
- Update outdated `BUILD_PROCESS.md` workflow reference.

### Phase 2 (lite profile)
- Add `lite` flavor and disable optional features (`acra`, markdown rendering, background work, heavy debug tools).

### Phase 3 (protocol scope tightening)
- Keep only required ExoPlayer protocol modules based on your real source list.

---

## 6) Minimal command set for a simpler workflow

- Fast local APK:
```bash
./gradlew :app:assembleDebug -DskipFormatKtlint
```

- Strict CI:
```bash
./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest -DskipFormatKtlint
./gradlew :app:runCheckstyle :app:runKtlint :app:checkDependenciesOrder
```

This keeps the old strict method, while giving you a simpler day-to-day build path.
