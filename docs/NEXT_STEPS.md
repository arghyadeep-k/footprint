# NEXT_STEPS

## Highest Priority

1. Run instrumentation tests on a machine with a connected emulator/device and fix any runtime failures:
   - `./gradlew :app:connectedDebugAndroidTest`
   - Cover: `LocationPointDaoIntegrationTest`, `LocationRepositoryTimelineIntegrationTest`, `FootprintMigrationScaffoldTest`, `PermissionExplanationScreenTest`, `HomeScreenPermissionGateTest`, and ACTIVE-trip guardrail runtime scenarios.
2. Add CI emulator job to gate `connectedDebugAndroidTest` in GitHub Actions.
3. Wire Google Maps API key manifest placeholder (`com.google.android.geo.API_KEY` with `${MAPS_API_KEY}`) and document secure local key provisioning.
4. Add focused ViewModel unit coverage for critical flows:
   - Home timeline loading/error/retry
   - Permission readiness/routing
   - Privacy action success/error transitions
   - export action state/result handling paths.
5. Add CSV import integration tests (repository/Room path), including duplicate-skip and summary accounting.
6. When database version increments beyond `1`, add and gate the first real migration test (`1->2`) and commit updated schema snapshots.

## Process Rule

- Every future task must append to `docs/WORK_DONE.md` and update `docs/NEXT_STEPS.md` so work can resume if context is lost.
