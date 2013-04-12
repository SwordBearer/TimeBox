package xmu.swordbearer.timebox.data;

import java.util.ArrayList;
import java.util.Calendar;

import xmu.swordbearer.timebox.alarm.Alarm;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.util.Log;

public class DataHandler {
	static String TAG = "DataHandler";

	// 根据某一状态下的所有任务
	public static Cursor getTaskByStatus(Context context, int task_status) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryTaskByStatus(task_status);
		// Log.e(TAG, "查询到的所有的任务有 " + cursor.getCount());
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	/**
	 * 计算一周内的任务
	 * 
	 * @param context
	 * @param today今天
	 * @return
	 */
	public static Cursor getTaskOfWeek(Context context, Calendar today) {
		return null;
	}

	/**
	 * 查询指定日期的任务
	 * 
	 * @param context
	 * @param calender
	 * @return
	 */
	public static Cursor getTaskByDate(Context context, Calendar calender) {
		/**
		 * 总共有三种任务:
		 * 
		 * @1.没有闹钟的任务;
		 * @2.有闹钟的任务(闹钟已经过时);
		 * @3.有闹钟的任务(闹钟未发生)
		 */
		String todayStr = CalendarUtil.calendar2LongString(calender);
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		/* @1.没有闹钟的任务; */
		String sql2 = "SELECT * FROM task_table where date(task_create_date)=date('"
				+ todayStr + "') AND task_alarm_id=-1 AND task_status!=-1";
		Cursor cursor1 = dbAdapter.query(sql2);
		Log.e(TAG, "没有闹钟的任务有 " + cursor1.getCount());
		// 查询有闹钟且属于该天的闹钟
		ArrayList<Alarm> alarms = AlarmHandler.calculateAlarmsOfDay(context,
				calender);
		int alarmCount = alarms.size();
		Log.e(TAG, " 这一天的闹钟有  " + alarms.size());
		int noAlarmTaskCount = cursor1.getCount() > 0 ? 1 : 0;
		if (alarmCount == 0 && noAlarmTaskCount == 0) {
			dbAdapter.close();
			return cursor1;
		}
		Cursor[] allCursors = new Cursor[alarmCount + noAlarmTaskCount];
		// 根据alarmId查询任务
		for (int i = 0; i < alarmCount; i++) {
			String sql = "SELECT * FROM " + DBAdapter.TASK_TABLE + " WHERE "
					+ TaskColumns.TASK_ALARM_ID + "=" + alarms.get(i).id
					+ " AND task_status!=-1 ";
			Cursor tempC = dbAdapter.query(sql);
			if (tempC.moveToFirst()) {
				allCursors[i] = tempC;
			}
		}
		if (allCursors.length == alarmCount + 1) {
			allCursors[alarmCount] = cursor1;
		}

		Cursor c = new MergeCursor(allCursors);
		c.moveToFirst();
		dbAdapter.close();
		return c;
	}

	private void testAlarm() {
		AlarmManager am;
	}

	/**
	 * 根据ID 查询一条记录
	 * 
	 * @param context
	 * @param table
	 * @param id
	 * @return
	 */
	public static Cursor queryById(Context context, String table, int id) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryById(table, id);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	public static Cursor queryTaskByAlarmId(Context context, int alarmId) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		String sql = "SELECT * FROM " + DBAdapter.TASK_TABLE + " WHERE "
				+ Task.TaskColumns.TASK_ALARM_ID + "=" + alarmId;
		Cursor cursor = dbAdapter.query(sql);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	public static Cursor getTask(Context context, String alarmDate) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryTaskByDate(alarmDate);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	public static void updateTask(Context context, int id, ContentValues values) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		dbAdapter.update(DBAdapter.TASK_TABLE, id, values);
		dbAdapter.close();
	}

	public static void updateTasks(DBAdapter dbAdapter, Cursor cursor,
			ContentValues values) {
		if (!cursor.moveToFirst()) {
			return;
		}
		do {
			dbAdapter.update(DBAdapter.TASK_TABLE, cursor.getInt(0), values);
		} while (cursor.moveToNext());
	}

	public static boolean delete(Context context, String table, int id) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		boolean res = dbAdapter.delete(table, id);
		dbAdapter.close();
		return res;
	}

	public static void updateTaskAlarm(Context context, int alarmId) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		dbAdapter.updateTaskAlarm(-1);
		dbAdapter.close();
	}

	public static Cursor getPlanTask(DBAdapter dbAdapter, int planId) {
		String sql = "SELECT * FROM " + DBAdapter.TASK_TABLE + " WHERE "
				+ TaskColumns.TASK_PLAN_ID + "=" + planId;
		Cursor cursor = dbAdapter.query(sql);
		// Log.e(TAG, "此计划中的任务总数  " + cursor.getCount());
		cursor.moveToFirst();
		return cursor;
	}

	public static Cursor getAllPlan(Context context, int plan_status) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryAllPlan(plan_status);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	public static Cursor getAllWord(Context context) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryByTable(DBAdapter.WORD_TABLE);
		cursor.moveToFirst();
		// Log.e(TAG, "All Word 总数 " + cursor.getCount());
		dbAdapter.close();
		return cursor;
	}

	public static Cursor getAllNote(Context context) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryByTable(DBAdapter.NOTE_TABLE);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}

	public static Cursor getAllStoptodo(Context context) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryByTable(DBAdapter.STOPTODO_TABLE);
		cursor.moveToFirst();
		dbAdapter.close();
		return cursor;
	}
}
