package xmu.swordbearer.timebox.data;

import java.util.Calendar;

import xmu.swordbearer.timebox.utils.CalendarUtil;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Plan {

	public static final int PLAN_STATUS_UNFINISHED = 0;
	public static final int PLAN_STATUS_FINISHED = 1;
	public static final int PLAN_STATUS_ALL = 2;// unfinished & finished

	public static class PlanColumns implements BaseColumns {
		public static final String PLAN_NAME = "plan_name";// 1
		public static final String PLAN_DETAIL = "plan_detail";// 2
		public static final String PLAN_START = "plan_start";// 3
		public static final String PLAN_STATUS = "plan_status";// 4 // 0, 1, -1
		//
		public static final String[] PLAN_DEFAULT_COLUMNS = { _ID, PLAN_NAME,
				PLAN_DETAIL, PLAN_START, PLAN_STATUS };
	}

	public int id;
	public String name;
	public String detail;
	public String startTime;
	public int status;

	public Plan() {
		id = -1;
		name = "";
		detail = "";
		status = PLAN_STATUS_UNFINISHED;
		startTime = CalendarUtil.calendar2LongString(Calendar.getInstance());
	}

	public Plan(Cursor cursor) {
		id = cursor.getInt(0);
		name = cursor.getString(1);
		detail = cursor.getString(2);
		startTime = cursor.getString(3);
		status = cursor.getInt(4);
	}
}
