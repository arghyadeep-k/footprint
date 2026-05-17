# Footprint

Footprint is a low-power, background location history Android app that stores travel points locally and draws travel routes on a map.

## Purpose

- Record location history with conservative defaults for battery use.
- Show route history over timeline ranges.
- Keep data local-first (no cloud sync in current scope).

## Current Status

- Kotlin + Jetpack Compose + Material 3 app foundation is in place.
- Foreground service tracking, timeline filtering, map polyline rendering, export, and privacy controls are implemented.
- Runtime tracking state is persisted/reconciled at startup.
- Room database is at version `1` with schema export enabled.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room + KSP
- Google Maps Compose + Play Services Location
- Min SDK: `26`
- Compile/Target SDK: `35`

## Required Tools

1. JDK: `21`
2. Android SDK with:
   - Android command-line tools
   - `platform-tools`
   - SDK Platform `android-35`
   - Build Tools `35.0.0` (or compatible installed by AGP)
3. Use project Gradle wrapper (`./gradlew`), do not rely on a globally installed Gradle.

## Local Machine Setup

1. Clone repository.
2. Create/update `local.properties` (machine-specific, do not commit changes):

```properties
sdk.dir=/absolute/path/to/your/android-sdk
```

3. Ensure `JAVA_HOME` points to JDK 21, for example:

```bash
export JAVA_HOME=/path/to/jdk-21
```

4. Optional: if `JAVA_HOME` is not picked up correctly, set Gradle Java home locally (machine-specific) in your user Gradle properties (`~/.gradle/gradle.properties`):

```properties
org.gradle.java.home=/absolute/path/to/jdk-21
```

Note: `local.properties` is already ignored by `.gitignore`.

## Build and Test Commands

From repo root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

CI (GitHub Actions) currently runs:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`

Planned next CI step:

- `./gradlew :app:connectedDebugAndroidTest` (requires emulator/device setup)

Device/emulator checks:

```bash
adb devices
```

Install debug APK on connected device/emulator:

```bash
./gradlew :app:installDebug
```

## Google Maps API Key Setup

Current status:

- `AndroidManifest.xml` does not yet define `com.google.android.geo.API_KEY` metadata.

Recommended local setup before shipping map features:

1. Add app metadata in `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

2. Provide `MAPS_API_KEY` via a local, non-committed source (for example user `~/.gradle/gradle.properties`):

```properties
MAPS_API_KEY=your_key_here
```

3. Do not commit real API keys to the repository.

Current implementation detail:

- The app does not yet wire `MAPS_API_KEY` manifest placeholders in source.
- Add the manifest metadata + local secret before production map rollout.

## Permission Expectations

Footprint expects these permissions for full tracking behavior:

- Foreground location:
  - `ACCESS_FINE_LOCATION` and/or `ACCESS_COARSE_LOCATION`
- Background location:
  - `ACCESS_BACKGROUND_LOCATION`
- Foreground service:
  - `FOREGROUND_SERVICE`
  - `FOREGROUND_SERVICE_LOCATION` (Android 14+)
- Notifications (Android 13+):
  - `POST_NOTIFICATIONS`

Permission flow is staged in-app:

1. Foreground location request first.
2. Background location guidance after foreground grant.
3. Notification permission requested separately on Android 13+.

## Local-First Privacy Expectations

- Location history is stored locally on the device (Room database).
- Export actions (CSV/GeoJSON) are user-initiated via Android document picker.
- Import actions (CSV restore) are user-initiated and local-only.
- No cloud sync is implemented in the current app.

## Database and Migration Policy

- Database: `FootprintDatabase`
- Version: `1`
- Exported schemas: `app/schemas`
- Policy and migration instructions: [`docs/DATABASE_POLICY.md`](docs/DATABASE_POLICY.md)
