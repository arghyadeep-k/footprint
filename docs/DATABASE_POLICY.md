# Database Policy (Room)

## Current Version

- Database: `FootprintDatabase`
- Current Room version: `1`
- Schema export location: `app/schemas`

## Migration Rules

1. Never use destructive migration in production builds.
2. Every schema-changing release must include explicit Room `Migration` objects.
3. Add new migrations in `FootprintMigrations` and register them in `FootprintDatabaseProvider` with `.addMigrations(...)`.
4. Keep migration paths contiguous (for example, `1->2`, `2->3`, etc.) so upgrades from older installs are safe.
5. Update schema JSON by running a build after entity/DAO/database changes.

## How To Add a New Migration

1. Increase `version` in `FootprintDatabase`.
2. Create migration object(s) in `FootprintMigrations` (for example `MIGRATION_1_2`).
3. Add migration object(s) to `FootprintMigrations.ALL` in order.
4. Run:

```bash
./gradlew :app:assembleDebug
```

5. Verify updated schema JSON under `app/schemas` and commit it.

## How To Test Migrations

- Baseline scaffold test: `FootprintMigrationScaffoldTest`.
- Add migration tests using `MigrationTestHelper`:
  - create old-version database with `helper.createDatabase(...)`
  - execute `helper.runMigrationsAndValidate(...)` with target version and migrations

Run tests with:

```bash
./gradlew :app:connectedDebugAndroidTest
```

(Requires a connected emulator/device.)
