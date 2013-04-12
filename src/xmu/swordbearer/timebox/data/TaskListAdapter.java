package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class TaskListAdapter extends BaseCursorAdapter {
	String TAG = "TaskListAdapter";

	public TaskListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.list_child_task, null, false);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.tasklist_child_check);
		final TextView nameTextView = (TextView) view
				.findViewById(R.id.tasklist_child_name);
		nameTextView.setText(cursor.getString(2));
		int task_status = cursor.getInt(1);
		if (task_status == Task.TASK_STATUS_FINISHED) {
			checkBox.setChecked(true);
			nameTextView.setTextColor(context.getResources().getColor(
					R.color.light_gray));
		} else {
			checkBox.setChecked(false);
			nameTextView.setTextColor(context.getResources().getColor(
					R.color.black));
		}
		final int tag_key1 = checkBox.getId();
		final int tag_key2 = tag_key1 + 1;
		checkBox.setTag(tag_key1, cursor.getInt(0));
		checkBox.setTag(tag_key2, cursor.getInt(6));
		checkBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ContentValues values = new ContentValues();
				int taskId = (Integer) checkBox.getTag(tag_key1);
				int alarmId = (Integer) checkBox.getTag(tag_key2);
				Log.e(TAG, "当前任务ID " + taskId + " 对应的闹钟ID " + alarmId);
				if (checkBox.isChecked()) {
					values.put(TaskColumns.TASK_STATUS,
							Task.TASK_STATUS_FINISHED);
					DataHandler.updateTask(context, taskId, values);
					// 任务完成，取消对应的闹钟
					AlarmHandler.enableAlarm(context, alarmId, false);
					nameTextView.setTextColor(context.getResources().getColor(
							R.color.light_gray));
				} else {
					values.put(TaskColumns.TASK_STATUS,
							Task.TASK_STATUS_UNFINISHED);
					DataHandler.updateTask(context, taskId, values);
					AlarmHandler.enableAlarm(context, alarmId, true);
					nameTextView.setTextColor(context.getResources().getColor(
							R.color.black));
				}

			}
		});
	}
}
