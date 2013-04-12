package xmu.swordbearer.timebox.utils;

import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.Items.RecordColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ScoreUtil {
	private static int total_time;
	private static int total_score;
	private static int total_share;
	private static int total_level;

	private static int total_task;
	private static int total_plan;
	private static int total_word;
	private static int portrait_id;

	public static void addScore(int score) {
		total_score += score;
	}

	public static void addTime(int time) {
		total_time += time;
	}

	public static void addShare() {
		++total_share;
	}

	public static void saveData(Context context) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		ContentValues values = new ContentValues();

		values.put(RecordColumns.RECORD_TIME, total_time);
		values.put(RecordColumns.RECORD_SCORE, total_score);
		values.put(RecordColumns.RECORD_SHARE, total_share);
		values.put(RecordColumns.RECORD_LEVEL, total_level);
		dbAdapter.saveData(values);
		dbAdapter.close();
	}

	public static void queryData(Context context) {
		DBAdapter dbAdapter = new DBAdapter(context);
		dbAdapter.open();
		Cursor cursor = dbAdapter.queryData();
		total_time = cursor.getInt(1);
		total_share = cursor.getInt(2);
		total_score = cursor.getInt(3);
		total_level = cursor.getInt(4);

		Cursor cursor2 = dbAdapter.queryTaskByStatus(2);
		total_task = cursor2.getCount();
		Cursor cursor3 = dbAdapter.queryAllPlan(2);
		total_plan = cursor3.getCount();

		Cursor cursor4 = dbAdapter.queryByTable(DBAdapter.WORD_TABLE);
		total_word = cursor4.getCount();

		cursor.close();
		cursor2.close();
		cursor3.close();
		cursor4.close();
		scoreToLevle();
	}

	private static void scoreToLevle() {
		if (total_score < 400) {
			total_level = 1;
			portrait_id = 1;
		} else if (total_score < 800) {
			total_level = 2;
			portrait_id = 2;
		} else if (total_score < 1500) {
			total_level = 3;
			portrait_id = 3;
		} else if (total_score < 2500) {
			total_level = 4;
			portrait_id = 4;
		} else if (total_score < 4000) {
			total_level = 5;
			portrait_id = 5;
		} else {
			total_level = 6;
			portrait_id = 5;
		}
	}

	public static int getTotal_time() {
		return total_time;
	}

	public static int getTotal_score() {
		return total_score;
	}

	public static int getTotal_share() {
		return total_share;
	}

	public static int getTotal_level() {
		return total_level;
	}

	public static int getTotal_task() {
		return total_task;
	}

	public static int getTotal_plan() {
		return total_plan;
	}

	public static int getTotal_word() {
		return total_word;
	}

	public static int getPortrait_id() {
		return portrait_id;
	}

}
