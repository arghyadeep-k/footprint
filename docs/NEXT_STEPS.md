# NEXT_STEPS

## Immediate

1. Add a second CI workflow/job for instrumentation coverage with emulator setup and gating for:
   `./gradlew :app:connectedDebugAndroidTest` (including `FootprintMigrationScaffoldTest`, `LocationPointDaoIntegrationTest`, and `LocationRepositoryTimelineIntegrationTest`).
2. Add unit tests for new ViewModels (`HomeViewModel`, `PermissionViewModel`, `SettingsViewModel`, `PrivacyViewModel`), including timeline loading/error/retry and privacy action state transitions.
3. Add Google Maps API key manifest placeholder wiring (`com.google.android.geo.API_KEY` with `${MAPS_API_KEY}`) and keep secret values local/non-committed.
4. Run instrumentation tests on emulator/device for migration scaffolding and existing integration tests:
   execute `FootprintMigrationScaffoldTest`, `LocationPointDaoIntegrationTest`, and `LocationRepositoryTimelineIntegrationTest` in CI gating.
5. Expand startup reconciliation robustness:
   add instrumentation coverage for app-relaunch/service-running, service-stopped, and permission-missing startup scenarios (including stale `RUNNING` correction paths).
6. Add instrumentation coverage for ACTIVE trip guardrails:
   verify explicit start/stop flows, timeout fallback after ~2 hours, and that ACTIVE is not persisted as default preferred mode.
7. Add instrumentation coverage for permission UX transitions across Android versions:
   verify denied/permanently-denied foreground behavior, Android 10+ background settings guidance, and Android 13+ notification permission staging.
8. Add first real migration test when database version increments (for example `1->2`) and commit both old/new schema JSON snapshots.
9. Run/expand unit tests for timeline, visit detection, export formatters, and tracking-state mapping/service transitions in CI once test tasks are wired.

## Foundation Follow-Ups

1. Expand Navigation Compose usage with back stack/state restoration polishing (deep links and nav state save/restore across process recreation).
2. Refine permission UX with explicit denied/permanently-denied states and Android-version-specific background location messaging.
3. Polish ACTIVE trip UX:
   show remaining active-trip time and clearer auto-timeout messaging in the tracking card/notification.
4. Improve service-backed tracking state robustness:
   add explicit error reason mapping, test coverage for state transitions, and startup reconciliation when app launches while service is already running.
5. Add map/timeline loading-state polish:
   consider non-blocking skeleton placeholders and optional pull-to-refresh while keeping current success/empty/error behavior.
6. Upgrade custom timeline UX with time-of-day pickers and quick presets (last 24h, last 7 days, last 30 days).
7. Add map path UX improvements:
   route start/end markers, color by tracking mode/speed, and optional simplification controls.
8. Improve visit detection quality:
   merge nearby visits, handle brief GPS drift, and tune radius/duration thresholds via settings.
9. Add richer places UI:
   tappable map markers linked to the places list and optional bottom sheet detail view.
10. Expand timeline integration coverage:
   add boundary-case tests (exact day/week/month/year edges, reversed custom ranges, and DST transitions).
11. Add lightweight chart or summary cards (distance/day, points/day) on top of current timeline stats.
12. Add stronger privacy controls:
   export local data, selective date-range deletion, and optional automatic retention policy settings.
13. Add import/restore or archive strategy for local exports (still local-only, no cloud sync).
14. Expand CI beyond baseline build/unit checks with lint and instrumentation coverage once emulator workflow is stable.

## Process Rule

- Every future task must append to `docs/WORK_DONE.md` and update `docs/NEXT_STEPS.md` so work can resume if context is lost.
