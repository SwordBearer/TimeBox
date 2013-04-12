package xmu.swordbearer.timebox.utils;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class CalendarUtil {
	static String TAG = "CalendarHandler";

	public static Calendar string2Calendar2(String string) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(string);
		} catch (ParseException e) {
			Log.e(TAG, "日期转换失败！！！");
		}
		calendar.setTime(date);
		return calendar;
	}

	public static String calendar2LongString(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
		return format.format(calendar.getTime());
	}

	public static String calendar2TimeString(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		return format.format(calendar.getTime());
	}

	public static String calendar2DateString(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(calendar.getTime());
	}

	public static String getWeekDay(Calendar calendar) {
		String[] weekdays = new DateFormatSymbols().getWeekdays();
		return weekdays[calendar.get(Calendar.DAY_OF_WEEK)];
	}
}
