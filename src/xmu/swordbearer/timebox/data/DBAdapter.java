package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.data.Items.NoteColumns;
import xmu.swordbearer.timebox.data.Items.RecordColumns;
import xmu.swordbearer.timebox.data.Items.StoptodoColumns;
import xmu.swordbearer.timebox.data.Plan.PlanColumns;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import xmu.swordbearer.timebox.data.Word.WordColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	String TAG = "DBAdapter";

	private static final String DB_NAME = "timebox.db";

	public static final String TASK_TABLE = "task_table";
	/* plan */
	public static final String PLAN_TABLE = "plan_table";
	/* user account */
	public static final String RECORD_TABLE = "account_table";
	/* word */
	public static final String WORD_TABLE = "word_table";
	/* note */
	public static final String NOTE_TABLE = "note_table";
	/* stoptodo */
	public static final String STOPTODO_TABLE = "stoptodo_table";
	private SQLiteDatabase db;
	private DBHelper dbHelper;

	public DBAdapter(Context context) {
		dbHelper = new DBHelper(context);
	}

	public void open() {
		if (null == db || !db.isOpen()) {
			try {
				db = dbHelper.getWritableDatabase();
			} catch (SQLiteException sqLiteException) {
				// Log.e(TAG, "数据库打开失败");
			}
		}
	}

	/*
	 * return: the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public boolean insert(String table, ContentValues values) {
		return (db.insert(table, null, values) != -1);
	}

	// delete record
	public boolean delete(String table, int id) {
		String[] idStr = { id + "" };
		return (db.delete(table, "_id=?", idStr) == 1);
	}

	// update values
	public boolean update(String table, int id, ContentValues values) {
		String[] idStr = { "" + id };
		return (db.update(table, values, "_id=?", idStr) > 0);
	}

	public void updateTaskAlarm(int alarmId) {
		String[] idStr = { "" + alarmId };
		ContentValues values = new ContentValues();
		values.put(TaskColumns.TASK_ALARM_ID, alarmId);
		db.update(TASK_TABLE, values, TaskColumns.TASK_ALARM_ID + "=?", idStr);
	}

	public Cursor queryTaskByStatus(int task_status) {
		// 完成+未完成
		if (task_status == 2) {
			String sqlStr = "select * from " + TASK_TABLE + " WHERE "
					+ TaskColumns.TASK_STATUS + "!=-1";
			return db.rawQuery(sqlStr, null);
		}
		String sqlStr = "select * " + " from " + TASK_TABLE + " where "
				+ TaskColumns.TASK_STATUS + "='" + task_status + "'";
		return db.rawQuery(sqlStr, null);
	}

	public Cursor queryTaskByDate(String alarmDate) {
		String sqlStr = "SELECT * FROM " + TASK_TABLE;
		return db.rawQuery(sqlStr, null);
	}

	public Cursor queryById(String table, int id) {
		String sql = "SELECT * FROM " + table + " WHERE _id=" + id;
		Cursor cursor = db.rawQuery(sql, null);
		// Log.e(TAG, "queryById  " + sql + " 结果 " + cursor.getCount());
		return cursor;
	}

	public void deleteTable(String table) {
		db.execSQL("drop table " + table);
	}

	public Cursor query(String sql) {
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	// 查询计划
	public Cursor queryAllPlan(int plan_status) {
		// 完成+未完成
		if (plan_status == 2) {
			return db.query(PLAN_TABLE, null, PlanColumns.PLAN_STATUS + "!=?",
					new String[] { "-1" }, null, null, PlanColumns.PLAN_START);
		}
		String sqlStr = "select * " + " from " + PLAN_TABLE + " where "
				+ PlanColumns.PLAN_STATUS + "='" + plan_status + "' order by "
				+ PlanColumns.PLAN_START;
		return db.rawQuery(sqlStr, null);
	}

	public Cursor queryByTable(String table) {
		return db.query(table, null, null, null, null, null, null);
	}

	// 查詢用戶数据
	public Cursor queryData() {
		return db.query(RECORD_TABLE, null, null, null, null, null, null);
	}

	public void saveData(ContentValues values) {
		String[] array = { "0" };
		db.update(RECORD_TABLE, values, "_id=?", array);
	}

	public boolean close() {
		if (db != null) {
			db.close();
		}
		return true;
	}

	private class DBHelper extends SQLiteOpenHelper {
		private static final int VERSION = 1;

		public DBHelper(Context context) {
			super(context, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String create_task_table = "create table if not exists "
					+ TASK_TABLE + "(" + TaskColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ TaskColumns.TASK_STATUS + " INTEGER NOT NULL,"
					+ TaskColumns.TASK_NAME + " TEXT NOT NULL,"
					+ TaskColumns.TASK_DETAIL + " TEXT,"
					+ TaskColumns.TASK_LEVEL + " INTEGER NOT NULL,"
					+ TaskColumns.TASK_PLAN_ID + " INTEGER,"
					+ TaskColumns.TASK_ALARM_ID + " INTEGER,"
					+ TaskColumns.TASK_CREATE_DATE + " DATE NOT NULL)";

			String create_data_table = "create table if not exists "
					+ RECORD_TABLE + "(" + RecordColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ RecordColumns.RECORD_TIME + " INTEGER NOT NULL,"
					+ RecordColumns.RECORD_SHARE + " INTEGER NOT NULL,"
					+ RecordColumns.RECORD_SCORE + " INTEGER NOT NULL,"
					+ RecordColumns.RECORD_LEVEL + " INTEGER NOT NULL)";

			String create_plan_table = "create table if not exists "
					+ PLAN_TABLE + "(" + PlanColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ PlanColumns.PLAN_NAME + " TEXT NOT NULL,"
					+ PlanColumns.PLAN_DETAIL + " TEXT,"
					+ PlanColumns.PLAN_START + " DATE NOT NULL,"
					+ PlanColumns.PLAN_STATUS + " INTEGER NOT NULL)";

			String create_word_table = "create table if not exists "
					+ WORD_TABLE + "(" + WordColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ WordColumns.WORD_DATE + " DATE NOT NULL,"
					+ WordColumns.WORD_CONTENT + " TEXT NOT NULL,"
					+ WordColumns.WORD_COLOR + " INTEGER NOT NULL)";
			String create_note_table = "CREATE TABLE IF NOT EXISTS "
					+ NOTE_TABLE + "(" + NoteColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ NoteColumns.NOTE_DATE + " DATE NOT NULL,"
					+ NoteColumns.NOTE_CONTENT + " TEXT,"
					+ NoteColumns.NOTE_COLOR + " INTEGER)";

			String create_stoptodo_table = "CREATE TABLE IF NOT EXISTS "
					+ STOPTODO_TABLE + "(" + StoptodoColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ StoptodoColumns.STOPTODO_DATE + " DATE NOT NULL,"
					+ StoptodoColumns.STOPTODO_DETAIL + " TEXT)";
			db.execSQL(create_task_table);
			db.execSQL(create_data_table);
			db.execSQL(create_plan_table);
			db.execSQL(create_word_table);
			db.execSQL(create_note_table);
			db.execSQL(create_stoptodo_table);
			initData(db);
		}

		private void initData(SQLiteDatabase db) {
			Log.e(TAG, "初始化数据1111");
			ContentValues values1 = new ContentValues();
			values1.put(TaskColumns.TASK_NAME, "测试任务");
			values1.put(TaskColumns.TASK_DETAIL, "任务详情");
			values1.put(TaskColumns.TASK_PLAN_ID, 1);
			values1.put(TaskColumns.TASK_LEVEL, 2);
			values1.put(TaskColumns.TASK_ALARM_ID, -1);
			values1.put(TaskColumns.TASK_STATUS, 0);
			values1.put(TaskColumns.TASK_CREATE_DATE, "2012-12-29 00:32:12");
			for (int i = 0; i < 4; i++) {
				db.insert(TASK_TABLE, null, values1);
			}
			Log.e(TAG, "初始化数据22222");
			ContentValues values2 = new ContentValues();
			values2.put(PlanColumns.PLAN_NAME, "测试计划");
			values2.put(PlanColumns.PLAN_DETAIL, "计划详情");
			values2.put(PlanColumns.PLAN_START, "2013-12-29 00:32:12");
			values2.put(PlanColumns.PLAN_STATUS, 0);
			for (int i = 0; i < 3; i++) {
				db.insert(PLAN_TABLE, null, values2);
			}
			Log.e(TAG, "初始化数据33333");
			ContentValues values3 = new ContentValues();
			values3.put(WordColumns.WORD_CONTENT, "慢慢来，比较快");
			values3.put(WordColumns.WORD_COLOR, 234242);
			values3.put(WordColumns.WORD_DATE, "2013-12-29 00:32:12");
			for (int i = 0; i < 9; i++) {
				db.insert(WORD_TABLE, null, values3);
			}
			Log.e(TAG, "初始化数据444");
			ContentValues values4 = new ContentValues();
			values4.put(NoteColumns.NOTE_CONTENT, "做人要有原则，这样就什么也不用怕");
			values4.put(NoteColumns.NOTE_COLOR, 234242);
			values4.put(NoteColumns.NOTE_DATE, "2013-12-29 00:32:12");
			for (int i = 0; i < 9; i++) {
				db.insert(NOTE_TABLE, null, values4);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
		}
	}
}
