package xmu.swordbearer.timebox.alarm;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AlarmProvider extends ContentProvider {

	private SQLiteOpenHelper helper;
	private static final int ALARMS = 1;
	private static final int ALARMS_ID = 2;
	private static final String ALARM_TABLE = "alarms";
	private static final String AUTHORITY_URI = "xmu.swordbearer.timebox";
	private static final UriMatcher uriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		uriMatcher.addURI(AUTHORITY_URI, "alarm", ALARMS);
		uriMatcher.addURI(AUTHORITY_URI, "alarm/#", ALARMS_ID);
	}

	@Override
	public boolean onCreate() {
		helper = new AlarmDBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Generate the body of the query
		int match = uriMatcher.match(uri);
		switch (match) {
		case ALARMS:
			qb.setTables(ALARM_TABLE);
			break;
		case ALARMS_ID:
			qb.setTables(ALARM_TABLE);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor ret = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);
		if (ret == null) {
			Log.e("AlarmProvider", "Alarms.query: failed");
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return ret;
	}

	// 不懂
	@Override
	public String getType(Uri uri) {
		int match = uriMatcher.match(uri);
		switch (match) {
		case ALARMS:
			return "vnd.android.cursor.dir/alarms";
		case ALARMS_ID:
			return "vnd.android.cursor.item/alarms";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (uriMatcher.match(uri) != ALARMS) {
			throw new IllegalArgumentException("Cannot insert into URL: " + uri);
		}
		ContentValues values = null;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		//
		if (!values.containsKey(Alarm.Columns.HOUR))
			values.put(Alarm.Columns.HOUR, 0);

		if (!values.containsKey(Alarm.Columns.MINUTES))
			values.put(Alarm.Columns.MINUTES, 0);

		if (!values.containsKey(Alarm.Columns.DAYS_OF_WEEK))
			values.put(Alarm.Columns.DAYS_OF_WEEK, 0);

		if (!values.containsKey(Alarm.Columns.ALARM_TIME))
			values.put(Alarm.Columns.ALARM_TIME, 0);

		if (!values.containsKey(Alarm.Columns.ENABLED))
			values.put(Alarm.Columns.ENABLED, 0);

		if (!values.containsKey(Alarm.Columns.VIBRATE))
			values.put(Alarm.Columns.VIBRATE, 1);

		if (!values.containsKey(Alarm.Columns.ALERT))
			values.put(Alarm.Columns.ALERT, "");
		//
		SQLiteDatabase db = helper.getWritableDatabase();
		long rowId = db.insert(ALARM_TABLE, null, values);

		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + uri);
		}
		Uri newUrl = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
				rowId);
		Log.e("tag", "通知刷新的闹钟URI是 " + newUrl.toString());
		getContext().getContentResolver().notifyChange(newUrl, null);
		return newUrl;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {
		case ALARMS:
			count = db.delete(ALARM_TABLE, where, whereArgs);
			break;
		case ALARMS_ID: {
			String segment = uri.getPathSegments().get(1);
			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}
			count = db.delete(ALARM_TABLE, where, whereArgs);
			break;
		}
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		long rowId = 0;
		int match = uriMatcher.match(uri);
		SQLiteDatabase dbDatabase = helper.getWritableDatabase();
		switch (match) {
		case ALARMS_ID: {
			String segment = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = dbDatabase
					.update(ALARM_TABLE, values, "_id=" + rowId, null);
			break;
		}
		default: {
			throw new UnsupportedOperationException("Cannot update URL: " + uri);
		}
		}
		Log.e("AlarmProvider", "*** notifyChange() rowId: " + rowId + " url "
				+ uri);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static class AlarmDBHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "alarms.db";
		private static final int DATABASE_VERSION = 1;

		public AlarmDBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + ALARM_TABLE + " ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "hour INTEGER, " + "minutes INTEGER, "
					+ "daysofweek INTEGER, " + "alarmtime INTEGER, "
					+ "enabled INTEGER, " + "vibrate INTEGER, " + "ring TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
			onCreate(db);
		}
	}
}
