package xmu.swordbearer.timebox.utils;

import xmu.swordbearer.timebox.data.CommonVar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.widget.Toast;

public class UiUtils {
	public static void updateMainList(Context context) {
		Intent broadcastIntent = new Intent(CommonVar.ACTION_UPDATE_LIST);
		context.sendBroadcast(broadcastIntent);
	}

	/**
	 * 是否全屏显示：在Activity的onResume()方法中调用
	 * 
	 * @param activity
	 */
	public static void showInFullscreen(Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(CommonVar.PREF_FULLSCREEN, Context.MODE_PRIVATE);
		boolean isFullscreen = prefs.getBoolean(CommonVar.KEY_FULLSCREEN, false);
		if (isFullscreen) {
			int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
			activity.getWindow().setFlags(flag, flag);
		}
	}

	public static void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
}
