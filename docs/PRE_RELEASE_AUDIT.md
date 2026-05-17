# PRE_RELEASE_AUDIT

Date: 2026-05-17
Scope: Footprint local-first beta readiness audit

## Ready

- Build and unit-test baseline is stable in current environment:
  - `:app:assembleDebug` passes
  - `:app:testDebugUnitTest` passes
- Permission UX is staged and version-aware:
  - Foreground location first
  - Background guidance after foreground grant
  - Notification permission staged for Android 13+
- Foreground service foundation is present and aligned with app goal:
  - `foregroundServiceType="location"`
  - persistent tracking notification
  - start/stop actions and cleanup of location updates
- Low-power/adaptive tracking modes are implemented:
  - `LOW_POWER`, `BALANCED`, `ACTIVE`
  - movement-aware mode adjustments
- ACTIVE trip guardrails are implemented:
  - explicit start action required
  - timeout/fallback behavior and separate preferred vs effective mode
- Timeline filtering, map polyline rendering, thinning, and visit detection are implemented with unit coverage.
- Local privacy controls are present:
  - pause tracking
  - delete all
  - delete older than threshold
  - date-range deletion
  - local retention policy
- Local export/import exists:
  - CSV and GeoJSON export
  - CSV import with validation and summary
- Room schema export is enabled (`app/schemas`) and documented migration policy exists.
- README setup instructions are present and usable for local build/test setup.
- CI workflow exists for build + unit tests (`.github/workflows/android-ci.yml`).

## Needs Work

- Instrumentation runtime execution is not currently verified in this environment (no connected device/emulator).
- Google Maps API key manifest placeholder is documented but not wired in `AndroidManifest.xml` yet.
- ViewModel-focused unit coverage is still partial (notably behavior/state transitions for full Home/Privacy/Permission/Settings flows).
- CSV import duplicate handling is practical but basic (timestamp + rounded lat/lng key); additional integration tests and edge-case hardening are advisable before wider beta.
- `docs/NEXT_STEPS.md` currently had duplicated numbering/content drift before this audit; prioritization cleanup is required.

## Blockers

1. No connected-device instrumentation verification:
   - `:app:connectedDebugAndroidTest` fails with `No connected devices!`
   - This blocks confidence in runtime behavior for service/permissions/Room migration flows on real Android runtime.
2. Maps API key manifest wiring is incomplete:
   - map feature can be build-complete but runtime map behavior is not release-ready without API key metadata + local secret setup.

## Recommended Next Fixes

1. Run and stabilize instrumentation on emulator/device, then gate it in CI (`connectedDebugAndroidTest`).
2. Add Google Maps API key manifest placeholder wiring (`com.google.android.geo.API_KEY` + `${MAPS_API_KEY}`) and keep key local/non-committed.
3. Add focused ViewModel tests for:
   - privacy action success/failure transitions
   - timeline loading/error/retry
   - permission readiness routing behavior.
4. Add CSV import integration tests (Room repository path + duplicate/skip summary accounting).
5. Add first real migration test when DB version increments (`1->2`) and keep schema snapshots committed.

## Manual Test Checklist

- Install debug app and open fresh install.
- Permissions flow:
  - deny foreground once
  - permanently deny foreground and verify settings CTA
  - grant foreground and verify background guidance appears next (not in same request)
  - on Android 13+, verify notification permission step is separate.
- Tracking control:
  - Start/stop normal tracking
  - verify persistent notification text
  - verify status card reflects service-backed state after app relaunch.
- ACTIVE trip:
  - explicit start required
  - explicit stop works
  - verify timeout/fallback behavior and messaging.
- Timeline + map:
  - switch all timeline presets including custom date/time
  - verify loading/empty/error/retry states
  - verify route polyline + start/end markers + visit markers.
- Privacy/data controls:
  - delete all
  - delete older than 30 days
  - delete custom date range
  - retention policy selection and effect after relaunch.
- Export/import:
  - export CSV + GeoJSON
  - import valid CSV
  - import CSV with invalid rows and verify summary/skip behavior.
- Database safety:
  - confirm existing history survives normal app upgrades (no destructive migration behavior introduced).
- Navigation:
  - verify back stack behavior for permissions/home/settings/privacy across rotation/relaunch.

## Command Results

Executed in this audit pass:

1. `./gradlew :app:assembleDebug`
   - Result: `BUILD SUCCESSFUL`
2. `./gradlew :app:testDebugUnitTest`
   - Result: `BUILD SUCCESSFUL`
3. `./gradlew :app:connectedDebugAndroidTest`
   - Result: `BUILD FAILED`
   - Failure: `com.android.builder.testing.api.DeviceException: No connected devices!`
