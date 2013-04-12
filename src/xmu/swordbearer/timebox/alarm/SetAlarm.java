package xmu.swordbearer.timebox.alarm;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

public class SetAlarm extends PreferenceActivity implements OnTimeSetListener {
	static String TAG = "SetAlarm";

	private CheckBoxPreference enablePref;
	private Preference timePref;
	private RepeatPreference repeatPref;
	private RingPreference ringPref;
	private CheckBoxPreference vibratePref;

	private int mId;
	private boolean mEnabled;
	private int mHour;
	private int mMinutes;

	private Alarm alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_alarm);
		getListView().setItemsCanFocus(true);

		addPreferencesFromResource(R.xml.alarm_prefs);

		initView();
		initData();
	}

	private void initView() {
		enablePref = (CheckBoxPreference) findPreference("enable");
		timePref = findPreference("time");
		ringPref = (RingPreference) findPreference("ring");
		repeatPref = (RepeatPreference) findPreference("repeat");
		vibratePref = (CheckBoxPreference) findPreference("vibrate");

		Button btnSave = (Button) findViewById(R.id.setalarm_save);
		btnSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mEnabled = true;
				Log.e(TAG, "单击保存按钮");
				saveAlarm();
				Intent intent = new Intent();
				intent.putExtra(TaskColumns.TASK_ALARM_ID, mId);
				setResult(CommonVar.ALARM_RESULTCODE_OK, intent);
				finish();
			}
		});
		Button btnCancel = (Button) findViewById(R.id.setalarm_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// setResult(CommonVar.ALARM_RESULTCODE_CANCEL);
				cancelSetAlarm();
				finish();
			}
		});
		Button btnDel = (Button) findViewById(R.id.setalarm_delete);
		btnDel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				deleteAlarm();
			}
		});
	}

	private void initData() {
		Intent i = this.getIntent();
		mId = i.getIntExtra(CommonVar.ALARM_ID, -1);
		Log.e(TAG, "当前闹钟的ID 是 " + mId);
		if (mId == -1) {// 新建闹钟
			findViewById(R.id.setalarm_delete).setVisibility(View.GONE);
			alarm = new Alarm();
		} else {
			alarm = AlarmHandler.getAlarm(getContentResolver(), mId);
			if (alarm == null) {
				Toast.makeText(this, "闹钟设置失败", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}
		updatePrefs(alarm);
	}

	private Uri getDefaultSystemRingtone() {
		return RingtoneManager.getActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE);
	}

	private void updatePrefs(Alarm alarm) {
		mId = alarm.id;
		mEnabled = alarm.enabled;
		mHour = alarm.hour;
		mMinutes = alarm.minutes;
		repeatPref.setDaysOfWeek(alarm.daysOfWeek);
		enablePref.setChecked(alarm.enabled);
		vibratePref.setChecked(alarm.vibrate);
		//
		if (alarm.alert == null) {
			ringPref.setRing(getDefaultSystemRingtone());
		} else {
			ringPref.setRing(alarm.alert);
		}
		updateTime();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == timePref) {
			new TimePickerDialog(this, this, mHour, mMinutes,
					DateFormat.is24HourFormat(this)).show();
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mHour = hourOfDay;
		mMinutes = minute;
		updateTime();
		enablePref.setChecked(true);
	}

	// 取消设置闹钟
	private void cancelSetAlarm() {
		// 如果闹钟是新建的，就删除
		if (mId == -1)
			AlarmHandler.deleteAlarm(this, mId);
		// 如果是正在编辑的闹钟，就通知还原
	}

	//
	private void deleteAlarm() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.delete_alarm)).setMessage(
				getString(R.string.delete_alarm_confirm));
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int w) {
						AlarmHandler.deleteAlarm(SetAlarm.this, mId);
						setResult(CommonVar.ALARM_RESULTCODE_DELETE);
						finish();
					}
				});
		builder.setNegativeButton(android.R.string.cancel, null).show();
	}

	private void updateTime() {
		timePref.setSummary(AlarmHandler.formatTime(this, mHour, mMinutes,
				repeatPref.getDaysOfWeek()));
	}

	private void saveAlarm() {
		alarm.id = mId;
		alarm.enabled = enablePref.isChecked();
		alarm.hour = mHour;
		alarm.minutes = mMinutes;
		alarm.daysOfWeek = repeatPref.getDaysOfWeek();
		alarm.vibrate = vibratePref.isChecked();
		alarm.alert = ringPref.onRestoreRingtone();
		long time = 0;
		if (alarm.id == -1) {// 新建
			Log.e(TAG, "新建闹钟");
			time = AlarmHandler.addAlarm(this, alarm);
			mId = alarm.id;
		} else {// 更新
			Log.e(TAG, "更新闹钟");
			time = AlarmHandler.setAlarm(this, alarm);
		}
		if (mEnabled) {
			AlarmHandler.popAlarmSetToast(this, time);
		}
	}
}
