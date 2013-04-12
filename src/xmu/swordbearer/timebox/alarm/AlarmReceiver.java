package xmu.swordbearer.timebox.alarm;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

public class AlarmReceiver extends BroadcastReceiver {
	static String TAG = "AlarmReceiver";

	private final static int STALE_WINDOW = 60 * 30 * 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (CommonVar.ALARM_KILLED.equals(intent.getAction())) {
			// The alarm has been killed, update the notification
			updateNotification(context,
					(Alarm) intent
							.getParcelableExtra(CommonVar.ALARM_INTENT_EXTRA),
					intent.getIntExtra(CommonVar.ALARM_KILLED_TIMEOUT, -1));
			return;
		} else if (CommonVar.CANCEL_SNOOZE.equals(intent.getAction())) {
			AlarmHandler.saveSnoozeAlert(context, -1, -1);
			return;
		} else if (!CommonVar.ALARM_ALERT_ACTION.equals(intent.getAction())) {
			// Unknown intent, bail.
			return;
		}

		Alarm alarm = null;
		byte[] data = intent.getByteArrayExtra(CommonVar.ALARM_RAW_DATA);
		if (data != null) {
			Parcel in = Parcel.obtain();
			in.unmarshall(data, 0, data.length);
			in.setDataPosition(0);
			alarm = Alarm.CREATOR.createFromParcel(in);
		}

		if (alarm == null) {
			AlarmHandler.setNextAlert(context);
			return;
		}

		// Disable the snooze alert if this alarm is the snooze.
		AlarmHandler.disableSnoozeAlert(context, alarm.id);
		if (!alarm.daysOfWeek.isRepeatSet()) {
			AlarmHandler.enableAlarm(context, alarm.id, false);
		} else {
			// Enable the next alert if there is one. The above call to
			// enableAlarm will call setNextAlert so avoid calling it twice.
			AlarmHandler.setNextAlert(context);
		}

		//
		long now = System.currentTimeMillis();
		// Always verbose to track down time change problems.
		if (now > alarm.time + STALE_WINDOW) {
			return;
		}
		// Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
		// pick it up.
		AlarmAlertWakeLock.acquireCpuWakeLock(context);
		// //
		// // /* Close dialogs and window shade */
		Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.sendBroadcast(closeDialog);

		// Play the alarm alert and vibrate the device.
		Intent playAlarm = new Intent(CommonVar.ALARM_ALERT_ACTION);
		playAlarm.putExtra(CommonVar.ALARM_INTENT_EXTRA, alarm);
		context.startService(playAlarm);

		// NEW: Embed the full-screen UI here. The notification manager will
		// take care of displaying it if it's OK to do so.
		Intent alarmAlert = new Intent(context, AlarmAlertActivity.class);
		alarmAlert.putExtra(CommonVar.ALARM_INTENT_EXTRA, alarm);
		alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(alarmAlert);
	}

	// public static void prepareNextAlarm(Context context, Alarm alarm) {
	// // Disable the snooze alert if this alarm is the snooze.
	// AlarmHandler.disableSnoozeAlert(context, alarm.id);
	// if (!alarm.daysOfWeek.isRepeatSet()) {
	// AlarmHandler.enableAlarm(context, alarm.id, false);
	// } else {
	// // Enable the next alert if there is one. The above call to
	// // enableAlarm will call setNextAlert so avoid calling it twice.
	// AlarmHandler.setNextAlert(context);
	// }
	// }

	private void updateNotification(Context context, Alarm alarm, int timeout) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// If the alarm is null, just cancel the notification.
		if (alarm == null) {
			return;
		}

		// Launch SetAlarm when clicked.
		Intent viewAlarm = new Intent(context, SetAlarm.class);
		viewAlarm.putExtra(CommonVar.ALARM_ID, alarm.id);
		PendingIntent intent = PendingIntent.getActivity(context, alarm.id,
				viewAlarm, 0);

		// Update the notification to indicate that the alert has been
		// silenced.
		String label = "闹钟到了";
		Notification n = new Notification(R.drawable.alarm_notify_icon, label,
				alarm.time);
		n.setLatestEventInfo(
				context,
				label,
				context.getString(R.string.alarm_alert_alert_silenced, timeout),
				intent);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		// We have to cancel the original notification since it is in the
		// ongoing section and we want the "killed" notification to be a plain
		// notification.
		nm.cancel(alarm.id);
		nm.notify(alarm.id, n);
	}
}
