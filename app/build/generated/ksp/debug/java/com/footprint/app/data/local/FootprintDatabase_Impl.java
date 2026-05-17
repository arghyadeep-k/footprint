package com.footprint.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FootprintDatabase_Impl extends FootprintDatabase {
  private volatile LocationPointDao _locationPointDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `location_points` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `accuracyMeters` REAL, `altitudeMeters` REAL, `speedMetersPerSecond` REAL, `bearingDegrees` REAL, `provider` TEXT, `recordedAtEpochMillis` INTEGER NOT NULL, `batteryPercent` INTEGER, `trackingMode` TEXT NOT NULL, `source` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2f2cb52e9266eb4772947ac2e7bfe3a7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `location_points`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsLocationPoints = new HashMap<String, TableInfo.Column>(12);
        _columnsLocationPoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("accuracyMeters", new TableInfo.Column("accuracyMeters", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("altitudeMeters", new TableInfo.Column("altitudeMeters", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("speedMetersPerSecond", new TableInfo.Column("speedMetersPerSecond", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("bearingDegrees", new TableInfo.Column("bearingDegrees", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("provider", new TableInfo.Column("provider", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("recordedAtEpochMillis", new TableInfo.Column("recordedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("batteryPercent", new TableInfo.Column("batteryPercent", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("trackingMode", new TableInfo.Column("trackingMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLocationPoints.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLocationPoints = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLocationPoints = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLocationPoints = new TableInfo("location_points", _columnsLocationPoints, _foreignKeysLocationPoints, _indicesLocationPoints);
        final TableInfo _existingLocationPoints = TableInfo.read(db, "location_points");
        if (!_infoLocationPoints.equals(_existingLocationPoints)) {
          return new RoomOpenHelper.ValidationResult(false, "location_points(com.footprint.app.data.local.LocationPoint).\n"
                  + " Expected:\n" + _infoLocationPoints + "\n"
                  + " Found:\n" + _existingLocationPoints);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "2f2cb52e9266eb4772947ac2e7bfe3a7", "6efd047e206cc05588f344e4f2e63f5d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "location_points");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `location_points`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(LocationPointDao.class, LocationPointDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public LocationPointDao locationPointDao() {
    if (_locationPointDao != null) {
      return _locationPointDao;
    } else {
      synchronized(this) {
        if(_locationPointDao == null) {
          _locationPointDao = new LocationPointDao_Impl(this);
        }
        return _locationPointDao;
      }
    }
  }
}
