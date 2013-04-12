package xmu.swordbearer.timebox.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmInitReceiver extends BroadcastReceiver {

	/**
	 * Sets alarm on ACTION_BOOT_COMPLETED. Resets alarm on TIME_SET,
	 * TIMEZONE_CHANGED 接受开机启动完成的广播， 设置闹钟，当时区改变也设置
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		// Remove the snooze alarm after a boot.
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			AlarmHandler.saveSnoozeAlert(context, -1, -1);
		}

		AlarmHandler.disableExpiredAlarms(context);
		AlarmHandler.setNextAlert(context);
	}
}
