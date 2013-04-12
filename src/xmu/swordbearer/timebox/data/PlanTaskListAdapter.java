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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class PlanTaskListAdapter extends BaseCursorAdapter {
	String TAG = "PlanTaskListAdapter";

	public OnItemChangedListener onItemChangedListener;

	public interface OnItemChangedListener {
		public void onItemChanged();
	}

	public PlanTaskListAdapter(Context context, Cursor c) {
		super(context, c);
		try {
			onItemChangedListener = (OnItemChangedListener) context;
		} catch (ClassCastException ccException) {
			Log.e(TAG, "OnItemChangedListener类型转换失败");
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.list_child_plan_task, null, false);
	}

	@Override
	public void bindView(View view, final Context context, final Cursor cursor) {
		final TextView nameTextView = (TextView) view
				.findViewById(R.id.plandetail_tasklist_child_name);
		nameTextView.setText(cursor.getString(2));
		final CheckBox checkBox = (CheckBox) view
				.findViewById(R.id.plandetail_tasklist_child_check);
		final Button btnDelete = (Button) view
				.findViewById(R.id.plandetail_tasklist_child_delete);
		//
		int taskId = cursor.getInt(0);
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
		// 保存task的ID
		final int tag_key1 = checkBox.getId();
		final int tag_key2 = tag_key1 + 1;
		checkBox.setTag(checkBox.getId(), taskId);
		checkBox.setTag(checkBox.getId() + 1, cursor.getInt(6));
		btnDelete.setTag(taskId);
		//
		checkBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int id = (Integer) checkBox.getTag(tag_key1);
				int alarmId = (Integer) checkBox.getTag(tag_key2);
				Log.e(TAG, "当前任务ID " + id + " 对应的闹钟ID " + alarmId);
				ContentValues values = new ContentValues();
				if (checkBox.isChecked()) {
					values.put(TaskColumns.TASK_STATUS,
							Task.TASK_STATUS_FINISHED);
					DataHandler.updateTask(context, id, values);
					// 任务完成，取消对应的闹钟
					AlarmHandler.enableAlarm(context, alarmId, false);
					nameTextView.setTextColor(context.getResources().getColor(
							R.color.light_gray));
				} else {
					values.put(TaskColumns.TASK_STATUS,
							Task.TASK_STATUS_UNFINISHED);
					DataHandler.updateTask(context, id, values);
					AlarmHandler.enableAlarm(context, alarmId, true);
					nameTextView.setTextColor(context.getResources().getColor(
							R.color.black));
				}
			}
		});
		btnDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int id = (Integer) btnDelete.getTag();
				ContentValues values = new ContentValues();
				values.put(TaskColumns.TASK_PLAN_ID, -1);
				DataHandler.updateTask(context, id, values);
				onItemChangedListener.onItemChanged();
			}
		});
	}
}
