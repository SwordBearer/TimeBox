package xmu.swordbearer.timebox.alarm;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.activity.TaskDetailActivity;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Task;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmAlertActivity extends Activity {
	private static final String TAG = "AlarmAlertActivity";
	private TextView tvTitle;
	private Button btnDismiss;
	private Button btnSnooze;

	private Alarm alarm;
	private static final int DEFAULT_SNOOZE_TIME_MINUTES = 1;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(CommonVar.ALARM_SNOOZE_ACTION)) {
				snooze();
			} else if (action.equals(CommonVar.ALARM_DISMISS_ACTION)) {
				dismiss(false);
			} else {
				Alarm a = intent
						.getParcelableExtra(CommonVar.ALARM_INTENT_EXTRA);
				if (a != null && alarm.id == a.id) {
					dismiss(true);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_alarm_alert);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		this.setTitle("闹钟到了");
		alarm = getIntent().getParcelableExtra(CommonVar.ALARM_INTENT_EXTRA);
		Task task = new Task(DataHandler.queryTaskByAlarmId(this, alarm.id));
		if (alarm == null || task == null) {
			finish();
			return;
		}
		//
		tvTitle = (TextView) findViewById(R.id.alarm_taskname);
		btnDismiss = (Button) findViewById(R.id.alarm_btn_dismiss);
		btnSnooze = (Button) findViewById(R.id.alarm_btn_snooze);
		tvTitle.setText(task.name);
		btnDismiss.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss(false);
			}
		});
		btnSnooze.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				snooze();
			}
		});

		// Register to get the alarm killed/snooze/dismiss intent.
		IntentFilter filter = new IntentFilter(CommonVar.ALARM_KILLED);
		filter.addAction(CommonVar.ALARM_SNOOZE_ACTION);
		filter.addAction(CommonVar.ALARM_DISMISS_ACTION);
		registerReceiver(receiver, filter);
	}

	/**
	 * this is called when a second alarm is triggered while a previous alert
	 * window is still active.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		alarm = intent.getParcelableExtra(CommonVar.ALARM_INTENT_EXTRA);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// If the alarm was deleted at some point, disable snooze.
		if (AlarmHandler.getAlarm(getContentResolver(), alarm.id) == null) {
			btnSnooze.setEnabled(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// No longer care about the alarm being killed.
		unregisterReceiver(receiver);
	}

	private void snooze() {
		if (!btnSnooze.isEnabled()) {
			dismiss(false);
			return;
		}
		int snoozeMinutes = DEFAULT_SNOOZE_TIME_MINUTES;
		long snoozeTime = System.currentTimeMillis()
				+ (1000 * 60 * snoozeMinutes);
		AlarmHandler.saveSnoozeAlert(this, alarm.id, snoozeTime);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(snoozeTime);

		// Notify the user that the alarm has been snoozed.
		Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
		cancelSnooze.setAction(CommonVar.CANCEL_SNOOZE);
		cancelSnooze.putExtra(CommonVar.ALARM_ID, alarm.id);
		PendingIntent broadcast = PendingIntent.getBroadcast(this, alarm.id,
				cancelSnooze, 0);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.alarm_notify_icon,
				"闹钟推迟5分钟", 0);
		n.setLatestEventInfo(
				this,
				"任务名称",
				getString(R.string.alarm_notify_snooze_text,
						AlarmHandler.formatTime(this, c)), broadcast);
		n.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		nm.notify(alarm.id, n);

		String displayTime = getString(R.string.alarm_alert_snooze_set,
				snoozeMinutes);
		Toast.makeText(this, displayTime, Toast.LENGTH_LONG).show();
		stopService(new Intent(CommonVar.ALARM_ALERT_ACTION));
		finish();
	}

	private void dismiss(boolean killed) {
		if (!killed) {
			stopService(new Intent(CommonVar.ALARM_ALERT_ACTION));
		}
		finish();
	}

	public void onBackPressed() {
		return;
	}

	private void goToTaskDetail(int alarmId) {
		Cursor cursor = DataHandler.queryTaskByAlarmId(this, alarmId);
		if (cursor.getCount() == 0) {
			Toast.makeText(this, "任务错误！！！", Toast.LENGTH_SHORT).show();
			dismiss(false);
			return;
		}
		int taskId = cursor.getInt(0);
		Log.e(TAG, "传递的taskId " + taskId);
		Intent intent = new Intent(this, TaskDetailActivity.class);
		intent.putExtra(TaskColumns._ID, taskId);
		startActivity(intent);
		stopService(new Intent(CommonVar.ALARM_ALERT_ACTION));
		finish();
	}
}
