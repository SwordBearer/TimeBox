package xmu.swordbearer.timebox.data;

public class CommonVar {
	public static final int ALARM_REQUESTCODE_SET_ALARM = 0x00;
	public static final int ALARM_RESULTCODE_OK = 0x01;
	public static final int ALARM_RESULTCODE_DELETE = 0x02;
	public static final int ALARM_RESULTCODE_CANCEL = 0x03;

	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";
	public static final String ALARM_ID = "alarm_id";

	public static final String TASK_BUNDLE = "task_bundle";
	public static final String TASK_EXTRA_ID = "task_extra_id";
	public static final int TASK_REQUESTCODE_EDIT = 0x01;
	public static final int TASK_RESULTCODE_OK = 0x02;

	public static final String ALARM_ALERT_ACTION = "xmu.swordbearer.timebox.ALARM_ALERT";

	public static final String ALARM_KILLED = "alarm_killed";
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
	public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";
	public static final String ALARM_DONE_ACTION = "com.cn.daming.deskclock.ALARM_DONE";
	public static final String CANCEL_SNOOZE = "cancel_snooze";

	public static final String ALARM_SNOOZE_ACTION = "xmu.swordbearer.timebox.ALARM_SNOOZE";
	public static final String ALARM_DISMISS_ACTION = "xmu.swordbearer.timebox.ALARM_DISMISS";

	public static final String PREFERENCES = "AlarmClock";
	public final static String PREF_SNOOZE_ID = "snooze_id";
	public final static String PREF_SNOOZE_TIME = "snooze_time";

	//
	public final static String ACTION_UPDATE_LIST = "xmu.swordbearer.timebox.activity.MainActivity.UpdateListReceiver";
	public static final String ALARM_ALERT_SILENT = "silent";

	public static final String PLAN_BUNDLE = "plan_bundle";
	public static final int PLAN_REQUESTCODE_EDIT = 0x11;
	public static final int PLAN_RESULTCODE_OK = 0x12;

	public static final String WORD_BUNDLE = "word_bundle";
	public static final String NOTE_BUNDLE = "note_bundle";
	public static final String STOP_BUNDLE = "stop_bundle";

	//
	public static final String PREF_FULLSCREEN = "timebox.pref_fullscreen";
	public static final String KEY_FULLSCREEN = "timebox.key_fullscreen";
}
