package xmu.swordbearer.timebox.alarm;

import java.util.ArrayList;
import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DataHandler;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class AlarmHandler {
	static String TAG = "AlarmHandler";

	private final static String DM12 = "E h:mm aa";
	private final static String DM24 = "E k:mm";
	public final static String M12 = "h:mm aa";
	public final static String M24 = "kk:mm";

	public static long addAlarm(Context context, Alarm alarm) {
		ContentValues values = createContentValues(alarm);
		Uri uri = context.getContentResolver().insert(
				Alarm.Columns.CONTENT_URI, values);
		alarm.id = (int) ContentUris.parseId(uri);

		long timeInMillis = calculateAlarm(alarm);
		if (alarm.enabled) {
			clearSnoozeIfNeeded(context, timeInMillis);
		}
		setNextAlert(context);
		return timeInMillis;

	}

	// 删除闹钟，同时更改对应的任务的alarm_id为-1
	public static void deleteAlarm(Context context, int alarmId) {
		if (alarmId < 0) {
			return;
		}
		ContentResolver contentResolver = context.getContentResolver();
		/* If alarm is snoozing, lose it */
		disableSnoozeAlert(context, alarmId);
		Uri uri = ContentUris
				.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
		contentResolver.delete(uri, "", null);
		DataHandler.updateTaskAlarm(context, alarmId);
		setNextAlert(context);
	}

	public static Cursor getAlarmsCursor(ContentResolver resolver, long alarmId) {
		return resolver
				.query(Alarm.Columns.CONTENT_URI,
						Alarm.Columns.ALARM_QUERY_COLUMNS, "_id=?",
						new String[] { alarmId + "" },
						Alarm.Columns.DEFAULT_SORT_ORDER);
	}

	// Private method to get a more limited set of alarms from the database.
	private static Cursor getFilteredAlarmsCursor(
			ContentResolver contentResolver) {
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
				null, null);
	}

	// myself 找到所有闹钟
	private static Cursor getAllAlarmsCursor(ContentResolver resolver) {
		return resolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
	}

	private static ContentValues createContentValues(Alarm alarm) {
		ContentValues values = new ContentValues(7);
		// Set the alarm_time value if this alarm does not repeat. This will be
		// used later to disable expire alarms.
		// long time = 0;
		// if (!alarm.daysOfWeek.isRepeatSet()) {
		// time = calculateAlarm(alarm);
		// }

		values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
		values.put(Alarm.Columns.HOUR, alarm.hour);
		values.put(Alarm.Columns.MINUTES, alarm.minutes);
		values.put(Alarm.Columns.ALARM_TIME, alarm.time);
		values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
		values.put(Alarm.Columns.VIBRATE, alarm.vibrate);

		// A null alert Uri indicates a silent alarm.
		values.put(Alarm.Columns.ALERT,
				alarm.alert == null ? CommonVar.ALARM_ALERT_SILENT
						: alarm.alert.toString());
		return values;
	}

	private static void clearSnoozeIfNeeded(Context context, long alarmTime) {
		// If this alarm fires before the next snooze, clear the snooze to
		// enable this alarm.
		SharedPreferences prefs = context.getSharedPreferences(
				CommonVar.PREFERENCES, 0);
		long snoozeTime = prefs.getLong(CommonVar.PREF_SNOOZE_TIME, 0);
		if (alarmTime < snoozeTime) {
			clearSnoozePreference(context, prefs);
		}
	}

	public static Alarm getAlarm(ContentResolver resolver, int alarmId) {
		Cursor cursor = resolver.query(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
		Alarm alarm = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				alarm = new Alarm(cursor);
			}
			cursor.close();
		}
		return alarm;
	}

	public static long setAlarm(Context context, Alarm alarm) {
		ContentValues values = createContentValues(alarm);
		ContentResolver resolver = context.getContentResolver();
		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);

		long timeInMillis = calculateAlarm(alarm);
		if (alarm.enabled) {
			disableSnoozeAlert(context, alarm.id);
			clearSnoozeIfNeeded(context, timeInMillis);
		}
		setNextAlert(context);
		return timeInMillis;
	}

	public static void enableAlarm(final Context context, final int id,
			boolean enabled) {
		if (id < 0) {
			return;
		}
		enableAlarmInternal(context, id, enabled);
		setNextAlert(context);
	}

	private static void enableAlarmInternal(final Context context,
			final int id, boolean enabled) {
		enableAlarmInternal(context,
				getAlarm(context.getContentResolver(), id), enabled);
	}

	private static void enableAlarmInternal(final Context context,
			final Alarm alarm, boolean enabled) {
		if (alarm == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues(2);
		values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

		// If we are enabling the alarm, calculate alarm time since the time
		// value in Alarm may be old.
		if (enabled) {
			long time = 0;
			if (!alarm.daysOfWeek.isRepeatSet()) {
				time = calculateAlarm(alarm);
			}
			values.put(Alarm.Columns.ALARM_TIME, time);
		} else {
			disableSnoozeAlert(context, alarm.id);
		}
		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);
	}

	public static Alarm calculateNextAlert(final Context context) {
		Alarm alarm = null;
		long minTime = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
		Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					Alarm a = new Alarm(cursor);
					if (a.time == 0) {
						a.time = calculateAlarm(a);
					} else if (a.time < now) {
						enableAlarmInternal(context, a, false);
						continue;
					}
					if (a.time < minTime) {
						minTime = a.time;
						alarm = a;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return alarm;
	}

	public static void disableExpiredAlarms(final Context context) {
		Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
		long now = System.currentTimeMillis();

		if (cur.moveToFirst()) {
			do {
				Alarm alarm = new Alarm(cur);
				// A time of 0 means this alarm repeats. If the time is
				// non-zero, check if the time is before now.
				if (alarm.time != 0 && alarm.time < now) {
					enableAlarmInternal(context, alarm, false);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	static boolean get24HourMode(final Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}

	static void saveNextAlarm(Context context, String timeStr) {
		Settings.System.putString(context.getContentResolver(),
				Settings.System.NEXT_ALARM_FORMATTED, timeStr);
	}

	public static String setDateFormat(Context context) {
		return DateFormat.is24HourFormat(context) ? AlarmHandler.M24
				: AlarmHandler.M12;
	}

	public static Calendar calculateAlarm(int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		int nowHour = c.get(Calendar.HOUR_OF_DAY);
		int nowMinute = c.get(Calendar.MINUTE);
		// 如果闹钟的时间小于当前时间，就设置为第二天
		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			// Log.e(TAG, "如果闹钟的时间小于当前时间，就设置为第二天");
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0) {
			c.add(Calendar.DAY_OF_WEEK, addDays);
		}
		return c;
	}

	public static void setNextAlert(final Context context) {
		if (!enableSnoozeAlert(context)) {
			Alarm alarm = calculateNextAlert(context);
			if (alarm != null) {
				enableAlert(context, alarm, alarm.time);
			} else {
				disableAlert(context);
			}
		}
	}

	private static boolean enableSnoozeAlert(final Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				CommonVar.PREFERENCES, 0);

		int id = prefs.getInt(CommonVar.PREF_SNOOZE_ID, -1);
		if (id == -1) {
			return false;
		}
		long time = prefs.getLong(CommonVar.PREF_SNOOZE_TIME, -1);

		// Get the alarm from the db.
		final Alarm alarm = getAlarm(context.getContentResolver(), id);
		if (alarm == null) {
			return false;
		}
		// The time in the database is either 0 (repeating) or a specific time
		// for a non-repeating alarm. Update this value so the AlarmReceiver
		// has the right time to compare.
		alarm.time = time;
		enableAlert(context, alarm, time);
		return true;
	}

	static void disableAlert(Context context) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				new Intent(CommonVar.ALARM_ALERT_ACTION),
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		setStatusBarIcon(context, false);
		saveNextAlarm(context, "");
	}

	static void disableSnoozeAlert(final Context context, final int id) {
		SharedPreferences prefs = context.getSharedPreferences(
				CommonVar.PREFERENCES, 0);
		int snoozeId = prefs.getInt(CommonVar.PREF_SNOOZE_ID, -1);
		if (snoozeId == -1) {
			// No snooze set, do nothing.
			return;
		} else if (snoozeId == id) {
			// This is the same id so clear the shared prefs.
			clearSnoozePreference(context, prefs);
		}
	}

	@SuppressLint("NewApi")
	static void saveSnoozeAlert(final Context context, final int id,
			final long time) {
		SharedPreferences prefs = context.getSharedPreferences(
				CommonVar.PREFERENCES, 0);
		if (id == -1) {
			clearSnoozePreference(context, prefs);
		} else {
			SharedPreferences.Editor ed = prefs.edit();
			ed.putInt(CommonVar.PREF_SNOOZE_ID, id);
			ed.putLong(CommonVar.PREF_SNOOZE_TIME, time);
			ed.apply();
		}
		// Set the next alert after updating the snooze.
		setNextAlert(context);
	}

	@SuppressLint("NewApi")
	private static void clearSnoozePreference(final Context context,
			final SharedPreferences prefs) {
		final int alarmId = prefs.getInt(CommonVar.PREF_SNOOZE_ID, -1);
		if (alarmId != -1) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(alarmId);
		}

		final SharedPreferences.Editor ed = prefs.edit();
		ed.remove(CommonVar.PREF_SNOOZE_ID);
		ed.remove(CommonVar.PREF_SNOOZE_TIME);
		ed.apply();
	}

	public static long calculateAlarm(Alarm alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek)
				.getTimeInMillis();
	}

	// myself
	/**
	 * 查询属于某一天的所有闹钟，借此来查找到任务
	 * 
	 * @param context
	 * @param calendar
	 *            查询日期
	 * @return
	 */
	public static ArrayList<Alarm> calculateAlarmsOfDay(Context context,
			Calendar calendar) {
		ArrayList<Alarm> alarmList = new ArrayList<Alarm>();
		// 将星期与闹钟的重复星期对应
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		day = (day == 1 ? 6 : day - 2);
		Log.e(TAG, "闹钟的星期是 " + day);
		//
		long durationOfDay = 24 * 60 * 60 * 1000;
		long startOfDay = calendar.getTimeInMillis();
		Cursor allAlarmsCursor = getAllAlarmsCursor(context
				.getContentResolver());
		//
		Log.e(TAG, "所有的闹钟有 " + allAlarmsCursor.getCount());
		if (allAlarmsCursor != null) {
			if (allAlarmsCursor.moveToFirst()) {
				do {
					Alarm alarm = new Alarm(allAlarmsCursor);
					long time = calculateAlarm(alarm);
					// long time = alarm.time;
					// 如果没有设置重复
					if (!alarm.daysOfWeek.isRepeatSet()) {
						long dur = time - startOfDay;
						Log.e(TAG, "有闹钟但是没有重复 " + dur + "   " + durationOfDay);
						if (dur > 0 && dur < durationOfDay) {
							alarmList.add(alarm);
						}
						// 如果有重复
					} else {
						if (alarm.daysOfWeek.isSet(day)) {
							alarmList.add(alarm);
						}
					}
				} while (allAlarmsCursor.moveToNext());
			}
		}
		allAlarmsCursor.close();
		return alarmList;
	}

	private static void enableAlert(Context context, final Alarm alarm,
			final long atTimeInMillis) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(CommonVar.ALARM_ALERT_ACTION);

		Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(CommonVar.ALARM_RAW_DATA, out.marshall());

		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

		setStatusBarIcon(context, true);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(atTimeInMillis);
		String timeString = formatDayAndTime(context, c);
		saveNextAlarm(context, timeString);
	}

	public static String formatTime(Context context, Calendar c) {
		String format = get24HourMode(context) ? M24 : M12;
		return ((c == null) ? "" : (String) DateFormat.format(format, c));
	}

	public static String formatTime(Context context, int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {
		Calendar calendar = AlarmHandler.calculateAlarm(hour, minute,
				daysOfWeek);
		return formatTime(context, calendar);
	}

	private static void setStatusBarIcon(Context context, boolean enabled) {
		Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
		alarmChanged.putExtra("alarmSet", enabled);
		context.sendBroadcast(alarmChanged);
	}

	private static String formatDayAndTime(Context context, Calendar c) {
		String format = get24HourMode(context) ? DM24 : DM12;
		return (c == null) ? "" : (String) DateFormat.format(format, c);
	}

	public static void popAlarmSetToast(Context context, long timeInMillis) {
		String toastText = formatToast(context, timeInMillis);
		Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
	}

	public static String formatToast(Context context, long timeInMillis) {
		long delta = timeInMillis - System.currentTimeMillis();
		long hours = delta / (1000 * 60 * 60);
		long minutes = delta / (1000 * 60) % 60;
		long days = hours / 24;
		hours = hours % 24;

		String daySeq = (days == 0) ? "" : (days == 1) ? context
				.getString(R.string.day) : context.getString(R.string.days,
				Long.toString(days));

		String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context
				.getString(R.string.minute) : context.getString(
				R.string.minutes, Long.toString(minutes));

		String hourSeq = (hours == 0) ? "" : (hours == 1) ? context
				.getString(R.string.hour) : context.getString(R.string.hours,
				Long.toString(hours));

		boolean dispDays = days > 0;
		boolean dispHour = hours > 0;
		boolean dispMinute = minutes > 0;

		int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0)
				| (dispMinute ? 4 : 0);

		String[] formats = context.getResources().getStringArray(
				R.array.alarm_set);
		return String.format(formats[index], daySeq, hourSeq, minSeq);
	}
}
