package xmu.swordbearer.timebox.fragment;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.alarm.Alarm;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Task;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class TaskDetailDialog extends DialogFragment {
	private Task task;

	public TaskDetailDialog(Task task) {
		this.task = task;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View parent = inflater.inflate(R.layout.frag_task_detail_dialog, null);
		initView(parent);
		return parent;
	}

	private void initView(View parent) {
		TextView nameTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_name);
		TextView detailTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_detail);
		TextView levelTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_level);
		TextView planTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_plan);
		TextView createTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_createtime);
		TextView alarmTimeTextView = (TextView) parent
				.findViewById(R.id.taskdetail_dialog_alarm_time_textview);
		View alarmView = parent.findViewById(R.id.taskdetail_dialog_alarm);
		TextView alarmTextView = (TextView) parent
				.findViewById(R.id.alarmlist_child_time);
		TextView repeatTextView = (TextView) parent
				.findViewById(R.id.alarmlist_child_repeat);
		Button btnBack = (Button) parent
				.findViewById(R.id.taskdetail_dialog_btn_back);
		final CheckBox alarmOnOff = (CheckBox) parent
				.findViewById(R.id.alarmlist_child__onoff);

		String[] levels = getResources().getStringArray(R.array.task_level);
		levelTextView.setText(levels[task.level]);

		nameTextView.setText(task.name);
		if (task.detail.length() != 0) {
			detailTextView.setText(task.detail);
		} else {
			detailTextView.setVisibility(View.GONE);
		}
		if (task.planId == -1) {
			parent.findViewById(R.id.taskdetail_dialog_plan_panel)
					.setVisibility(View.GONE);
		} else {
			Cursor cursor = DataHandler.queryById(getActivity(),
					DBAdapter.PLAN_TABLE, task.planId);
			planTextView.setText(cursor.getString(1));
			cursor.close();
		}
		createTextView.setText(task.createTime.substring(0, 16));
		btnBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
		if (task.alarmId == -1) {
			alarmTimeTextView.setVisibility(View.GONE);
			alarmView.setVisibility(View.GONE);
		} else {
			final Alarm alarm = AlarmHandler.getAlarm(getActivity()
					.getContentResolver(), task.alarmId);
			if (alarm == null) {
				Toast.makeText(getActivity(), "闹钟查询错误", Toast.LENGTH_SHORT)
						.show();
				alarmTimeTextView.setVisibility(View.GONE);
				alarmView.setVisibility(View.GONE);
				return;
			}
			alarmTimeTextView.setVisibility(View.VISIBLE);
			alarmView.setVisibility(View.VISIBLE);
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			alarmTextView.setText(DateFormat.format(
					AlarmHandler.setDateFormat(getActivity()), c));
			repeatTextView.setText(alarm.daysOfWeek.toString(getActivity(),
					true));
			alarmOnOff.setChecked(alarm.enabled);
			alarmOnOff.setClickable(false);
		}
	}
}
