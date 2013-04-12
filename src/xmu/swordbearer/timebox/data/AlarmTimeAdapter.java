package xmu.swordbearer.timebox.data;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.alarm.Alarm;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.alarm.SetAlarm;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmTimeAdapter extends CursorAdapter {
	private Context context;
	private LayoutInflater inflater;

	public AlarmTimeAdapter(Context context, Cursor c) {
		super(context, c);
		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		final Alarm alarm = new Alarm(cursor);
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, alarm.hour);
		c.set(Calendar.MINUTE, alarm.minutes);
		TextView mTime = (TextView) view
				.findViewById(R.id.alarmlist_child_time);
		TextView mRepeat = (TextView) view
				.findViewById(R.id.alarmlist_child_repeat);
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.alarmlist_child__onoff);
		mTime.setText(DateFormat.format(AlarmHandler.setDateFormat(context), c));
		mRepeat.setText(alarm.daysOfWeek.toString(context, true));
		checkBox.setChecked(alarm.enabled);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				AlarmHandler.enableAlarm(context, alarm.id, true);
				if (checkBox.isChecked()) {
					Toast.makeText(context,
							AlarmHandler.formatToast(context, alarm.time),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(context, SetAlarm.class);
				intent.putExtra(CommonVar.ALARM_ID, alarm.id);
				// 设置闹钟
				((Activity) context).startActivityForResult(intent,
						CommonVar.ALARM_REQUESTCODE_SET_ALARM);
			}
		});
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
		View ret = inflater.inflate(R.layout.list_child_alarm, parent, false);
		return ret;
	}
}