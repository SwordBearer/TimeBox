package xmu.swordbearer.timebox.data;

import java.util.Calendar;

import xmu.swordbearer.timebox.utils.CalendarUtil;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Task {
	public int id;
	public String name;
	public String detail;
	public int level;
	public int status;
	public int planId;
	public int alarmId;
	public String createTime;

	public static final int TASK_STATUS_UNFINISHED = 0;
	public static final int TASK_STATUS_FINISHED = 1;
	public static final int TASK_STATUS_ALL = 2;// unfinished & finished

	// ////////
	public static final int TASK_LEVEL_EMERGENCY = 0;
	public static final int TASK_LEVEL_IMPORTANT = 1;
	public static final int TASK_LEVEL_GENERAL = 2;
	public static final int TASK_LEVEL_LOW = 3;

	public Task() {
		id = -1;
		name = "";
		detail = "";
		planId = -1;
		alarmId = -1;
		level = Task.TASK_LEVEL_GENERAL;
		status = Task.TASK_STATUS_UNFINISHED;
		createTime = CalendarUtil.calendar2LongString(Calendar.getInstance());
	}

	public Task(Cursor cursor) {
		id = cursor.getInt(0);
		status = cursor.getInt(1);
		name = cursor.getString(2);
		detail = cursor.getString(3);
		level = cursor.getInt(4);
		planId = cursor.getInt(5);
		alarmId = cursor.getInt(6);
		createTime = cursor.getString(7);
	}

	public static class TaskColumns implements BaseColumns {
		/* task */
		// task_status:0 unfinished ,1 finished,-1 deleted,2 all
		public static final String _ID = "_id";
		public static final String TASK_STATUS = "task_status"; // 1
		public static final String TASK_NAME = "task_name"; // 2
		public static final String TASK_DETAIL = "task_detail"; // 3
		public static final String TASK_LEVEL = "task_level"; // 4
		public static final String TASK_PLAN_ID = "task_plan_id"; // 5
		public static final String TASK_ALARM_ID = "task_alarm_id";// 6
		public static final String TASK_CREATE_DATE = "task_create_date";// 7
		public static final String[] TASK_DEFAULT_COLUMNS = { _ID, TASK_STATUS,
				TASK_NAME, TASK_DETAIL, TASK_LEVEL, TASK_PLAN_ID };
	}
}
