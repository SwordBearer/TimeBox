package xmu.swordbearer.timebox.alarm;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class Alarm implements Parcelable {

	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
		public Alarm createFromParcel(Parcel p) {
			return new Alarm(p);
		}

		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
	};
	public int id;
	public boolean enabled;
	public int hour;
	public int minutes;
	public DaysOfWeek daysOfWeek;
	public long time;
	public boolean vibrate;
	public Uri alert;
	public boolean silent;

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
		p.writeInt(vibrate ? 1 : 0);
		p.writeParcelable(alert, flags);
		p.writeInt(silent ? 1 : 0);
	}

	public static class Columns implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.parse("content://xmu.swordbearer.timebox/alarm");
		public static final String HOUR = "hour";
		public static final String MINUTES = "minutes";
		public static final String DAYS_OF_WEEK = "daysofweek";
		public static final String ALARM_TIME = "alarmtime";
		public static final String ENABLED = "enabled";
		public static final String VIBRATE = "vibrate";
		public static final String ALERT = "ring";
		public static final String DEFAULT_SORT_ORDER = HOUR + "," + MINUTES
				+ " ASC";
		// Used when filtering enabled alarms.
		public static final String WHERE_ENABLED = ENABLED + "=1";
		public static final String[] ALARM_QUERY_COLUMNS = { _ID, HOUR,
				MINUTES, DAYS_OF_WEEK, ALARM_TIME, ENABLED, VIBRATE, ALERT };

		public static final String ALARM_ALERT_SILENT = "silent";
		/**
		 * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT
		 * IN SYNC WITH ABOVE QUERY COLUMNS
		 */
		public static final int ALARM_ID_INDEX = 0;
		public static final int ALARM_HOUR_INDEX = 1;
		public static final int ALARM_MINUTES_INDEX = 2;
		public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
		public static final int ALARM_TIME_INDEX = 4;
		public static final int ALARM_ENABLED_INDEX = 5;
		public static final int ALARM_VIBRATE_INDEX = 6;
		public static final int ALARM_ALERT_INDEX = 7;

	}

	public Alarm(Cursor c) {
		id = c.getInt(Columns.ALARM_ID_INDEX);
		enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
		hour = c.getInt(Columns.ALARM_HOUR_INDEX);
		minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
		daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
		time = c.getLong(Columns.ALARM_TIME_INDEX);
		vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
		String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
		if (CommonVar.ALARM_ALERT_SILENT.equals(alertString)) {
			silent = true;
		} else {
			if (alertString != null && alertString.length() != 0) {
				alert = Uri.parse(alertString);
			}

			// If the database alert is null or it failed to parse, use the
			// default alert.
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
	}

	public Alarm(Parcel p) {
		id = p.readInt();
		enabled = p.readInt() == 1;
		hour = p.readInt();
		minutes = p.readInt();
		daysOfWeek = new DaysOfWeek(p.readInt());
		time = p.readLong();
		vibrate = p.readInt() == 1;
		alert = (Uri) p.readParcelable(null);
		silent = p.readInt() == 1;
	}

	public Alarm() {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		vibrate = true;
		daysOfWeek = new DaysOfWeek(0);
		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	}

	/*
	 * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
	 * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
	 * Sunday
	 */
	public static final class DaysOfWeek {
		private static int[] DAY_MAP = new int[] { Calendar.MONDAY,
				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };
		// Bitmask of all repeating days
		private int mDays;

		DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();
			Resources res = context.getResources();
			// no days
			if (mDays == 0) {
				return showNever ? res.getString(R.string.str_no_repeat) : "";
			}

			// every day
			if (mDays == 0x7f) {
				return res.getString(R.string.str_every_day);
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1)
					dayCount++;
				days >>= 1;
			}

			// short or long form?
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs
					.getWeekdays();

			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					ret.append(dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(", ");
				}
			}
			return ret.toString();
		}

		public boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

		public void set(DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public int getCoded() {
			return mDays;
		}

		// Returns days of week encoded in an array of booleans.
		public boolean[] getBooleanArray() {
			boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

		/**
		 * returns number of days from today until next alarm
		 * 
		 * @param c
		 *            must be set to today
		 */
		public int getNextAlarm(Calendar c) {
			if (mDays == 0) {
				return -1;
			}
			int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

			int day = 0;
			int dayCount = 0;
			for (; dayCount < 7; dayCount++) {
				day = (today + dayCount) % 7;
				if (isSet(day)) {
					break;
				}
			}
			return dayCount;
		}
	}
}
