package com.footprint.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LocationPointDao_Impl implements LocationPointDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LocationPoint> __insertionAdapterOfLocationPoint;

  private final SharedSQLiteStatement __preparedStmtOfDeletePointsOlderThan;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllPoints;

  public LocationPointDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLocationPoint = new EntityInsertionAdapter<LocationPoint>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `location_points` (`id`,`latitude`,`longitude`,`accuracyMeters`,`altitudeMeters`,`speedMetersPerSecond`,`bearingDegrees`,`provider`,`recordedAtEpochMillis`,`batteryPercent`,`trackingMode`,`source`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LocationPoint entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getLatitude());
        statement.bindDouble(3, entity.getLongitude());
        if (entity.getAccuracyMeters() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getAccuracyMeters());
        }
        if (entity.getAltitudeMeters() == null) {
          statement.bindNull(5);
        } else {
          statement.bindDouble(5, entity.getAltitudeMeters());
        }
        if (entity.getSpeedMetersPerSecond() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getSpeedMetersPerSecond());
        }
        if (entity.getBearingDegrees() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getBearingDegrees());
        }
        if (entity.getProvider() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getProvider());
        }
        statement.bindLong(9, entity.getRecordedAtEpochMillis());
        if (entity.getBatteryPercent() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getBatteryPercent());
        }
        statement.bindString(11, entity.getTrackingMode());
        statement.bindString(12, entity.getSource());
      }
    };
    this.__preparedStmtOfDeletePointsOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM location_points WHERE recordedAtEpochMillis < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllPoints = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM location_points";
        return _query;
      }
    };
  }

  @Override
  public Object insertLocationPoint(final LocationPoint locationPoint,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfLocationPoint.insertAndReturnId(locationPoint);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertLocationPoints(final List<LocationPoint> locationPoints,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfLocationPoint.insertAndReturnIdsList(locationPoints);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePointsOlderThan(final long olderThanEpochMillis,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePointsOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, olderThanEpochMillis);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePointsOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllPoints(final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllPoints.acquire();
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllPoints.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPointsBetween(final long startEpochMillis, final long endEpochMillis,
      final Continuation<? super List<LocationPoint>> $completion) {
    final String _sql = "\n"
            + "        SELECT *\n"
            + "        FROM location_points\n"
            + "        WHERE recordedAtEpochMillis BETWEEN ? AND ?\n"
            + "        ORDER BY recordedAtEpochMillis ASC, id ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpochMillis);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endEpochMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LocationPoint>>() {
      @Override
      @NonNull
      public List<LocationPoint> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfAccuracyMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "accuracyMeters");
          final int _cursorIndexOfAltitudeMeters = CursorUtil.getColumnIndexOrThrow(_cursor, "altitudeMeters");
          final int _cursorIndexOfSpeedMetersPerSecond = CursorUtil.getColumnIndexOrThrow(_cursor, "speedMetersPerSecond");
          final int _cursorIndexOfBearingDegrees = CursorUtil.getColumnIndexOrThrow(_cursor, "bearingDegrees");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final int _cursorIndexOfRecordedAtEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAtEpochMillis");
          final int _cursorIndexOfBatteryPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "batteryPercent");
          final int _cursorIndexOfTrackingMode = CursorUtil.getColumnIndexOrThrow(_cursor, "trackingMode");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final List<LocationPoint> _result = new ArrayList<LocationPoint>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LocationPoint _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final Float _tmpAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfAccuracyMeters)) {
              _tmpAccuracyMeters = null;
            } else {
              _tmpAccuracyMeters = _cursor.getFloat(_cursorIndexOfAccuracyMeters);
            }
            final Double _tmpAltitudeMeters;
            if (_cursor.isNull(_cursorIndexOfAltitudeMeters)) {
              _tmpAltitudeMeters = null;
            } else {
              _tmpAltitudeMeters = _cursor.getDouble(_cursorIndexOfAltitudeMeters);
            }
            final Float _tmpSpeedMetersPerSecond;
            if (_cursor.isNull(_cursorIndexOfSpeedMetersPerSecond)) {
              _tmpSpeedMetersPerSecond = null;
            } else {
              _tmpSpeedMetersPerSecond = _cursor.getFloat(_cursorIndexOfSpeedMetersPerSecond);
            }
            final Float _tmpBearingDegrees;
            if (_cursor.isNull(_cursorIndexOfBearingDegrees)) {
              _tmpBearingDegrees = null;
            } else {
              _tmpBearingDegrees = _cursor.getFloat(_cursorIndexOfBearingDegrees);
            }
            final String _tmpProvider;
            if (_cursor.isNull(_cursorIndexOfProvider)) {
              _tmpProvider = null;
            } else {
              _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            }
            final long _tmpRecordedAtEpochMillis;
            _tmpRecordedAtEpochMillis = _cursor.getLong(_cursorIndexOfRecordedAtEpochMillis);
            final Integer _tmpBatteryPercent;
            if (_cursor.isNull(_cursorIndexOfBatteryPercent)) {
              _tmpBatteryPercent = null;
            } else {
              _tmpBatteryPercent = _cursor.getInt(_cursorIndexOfBatteryPercent);
            }
            final String _tmpTrackingMode;
            _tmpTrackingMode = _cursor.getString(_cursorIndexOfTrackingMode);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            _item = new LocationPoint(_tmpId,_tmpLatitude,_tmpLongitude,_tmpAccuracyMeters,_tmpAltitudeMeters,_tmpSpeedMetersPerSecond,_tmpBearingDegrees,_tmpProvider,_tmpRecordedAtEpochMillis,_tmpBatteryPercent,_tmpTrackingMode,_tmpSource);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllPointsOrderedByTime(
      final Continuation<? super List<LocationPoint>> $completion) {
    final String _sql = "\n"
            + "        SELECT `location_points`.`id` AS `id`, `location_points`.`latitude` AS `latitude`, `location_points`.`longitude` AS `longitude`, `location_points`.`accuracyMeters` AS `accuracyMeters`, `location_points`.`altitudeMeters` AS `altitudeMeters`, `location_points`.`speedMetersPerSecond` AS `speedMetersPerSecond`, `location_points`.`bearingDegrees` AS `bearingDegrees`, `location_points`.`provider` AS `provider`, `location_points`.`recordedAtEpochMillis` AS `recordedAtEpochMillis`, `location_points`.`batteryPercent` AS `batteryPercent`, `location_points`.`trackingMode` AS `trackingMode`, `location_points`.`source` AS `source`\n"
            + "        FROM location_points\n"
            + "        ORDER BY recordedAtEpochMillis ASC, id ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LocationPoint>>() {
      @Override
      @NonNull
      public List<LocationPoint> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfLatitude = 1;
          final int _cursorIndexOfLongitude = 2;
          final int _cursorIndexOfAccuracyMeters = 3;
          final int _cursorIndexOfAltitudeMeters = 4;
          final int _cursorIndexOfSpeedMetersPerSecond = 5;
          final int _cursorIndexOfBearingDegrees = 6;
          final int _cursorIndexOfProvider = 7;
          final int _cursorIndexOfRecordedAtEpochMillis = 8;
          final int _cursorIndexOfBatteryPercent = 9;
          final int _cursorIndexOfTrackingMode = 10;
          final int _cursorIndexOfSource = 11;
          final List<LocationPoint> _result = new ArrayList<LocationPoint>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LocationPoint _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final Float _tmpAccuracyMeters;
            if (_cursor.isNull(_cursorIndexOfAccuracyMeters)) {
              _tmpAccuracyMeters = null;
            } else {
              _tmpAccuracyMeters = _cursor.getFloat(_cursorIndexOfAccuracyMeters);
            }
            final Double _tmpAltitudeMeters;
            if (_cursor.isNull(_cursorIndexOfAltitudeMeters)) {
              _tmpAltitudeMeters = null;
            } else {
              _tmpAltitudeMeters = _cursor.getDouble(_cursorIndexOfAltitudeMeters);
            }
            final Float _tmpSpeedMetersPerSecond;
            if (_cursor.isNull(_cursorIndexOfSpeedMetersPerSecond)) {
              _tmpSpeedMetersPerSecond = null;
            } else {
              _tmpSpeedMetersPerSecond = _cursor.getFloat(_cursorIndexOfSpeedMetersPerSecond);
            }
            final Float _tmpBearingDegrees;
            if (_cursor.isNull(_cursorIndexOfBearingDegrees)) {
              _tmpBearingDegrees = null;
            } else {
              _tmpBearingDegrees = _cursor.getFloat(_cursorIndexOfBearingDegrees);
            }
            final String _tmpProvider;
            if (_cursor.isNull(_cursorIndexOfProvider)) {
              _tmpProvider = null;
            } else {
              _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            }
            final long _tmpRecordedAtEpochMillis;
            _tmpRecordedAtEpochMillis = _cursor.getLong(_cursorIndexOfRecordedAtEpochMillis);
            final Integer _tmpBatteryPercent;
            if (_cursor.isNull(_cursorIndexOfBatteryPercent)) {
              _tmpBatteryPercent = null;
            } else {
              _tmpBatteryPercent = _cursor.getInt(_cursorIndexOfBatteryPercent);
            }
            final String _tmpTrackingMode;
            _tmpTrackingMode = _cursor.getString(_cursorIndexOfTrackingMode);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            _item = new LocationPoint(_tmpId,_tmpLatitude,_tmpLongitude,_tmpAccuracyMeters,_tmpAltitudeMeters,_tmpSpeedMetersPerSecond,_tmpBearingDegrees,_tmpProvider,_tmpRecordedAtEpochMillis,_tmpBatteryPercent,_tmpTrackingMode,_tmpSource);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
