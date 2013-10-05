package xmu.swordbearer.timebox.activity;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Task;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import xmu.swordbearer.timebox.fragment.CategoryFrag.Category;
import xmu.swordbearer.timebox.fragment.DataListFrag;
import xmu.swordbearer.timebox.fragment.TaskDetailFrag;
import xmu.swordbearer.timebox.utils.UiUtils;
import xmu.swordbearer.timebox.view.PopupMenu;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class TaskDetailActivity extends FragmentActivity implements
		android.view.View.OnClickListener {
	String TAG = "TaskDetail";

	private int cur_position = -1;
	private int cur_category = 0;
	private Cursor cursor;

	private ImageButton btnBack;
	private ImageButton btnMenu;
	private Button btnPrev;
	private Button btnNext;
	public static final String CUR_TASK_POS = "cur_task_pos";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);
		Intent intent = getIntent();
		cur_category = intent.getIntExtra(DataListFrag.CATEGORY, -1);
		cur_position = intent.getIntExtra(CUR_TASK_POS, -1);
		if (cur_category == -1) {
			finish();
			return;
		}
		if (cur_position == -1) {
			finish();
			return;
		}
		btnBack = (ImageButton) findViewById(R.id.taskdetail_btn_back);
		btnMenu = (ImageButton) findViewById(R.id.taskdetail_btn_menu);
		btnPrev = (Button) findViewById(R.id.btn_previous);
		btnNext = (Button) findViewById(R.id.btn_next);

		btnBack.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
		btnPrev.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		initData();
	}

	private void initData() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		switch (cur_category) {
		case Category.CATEGORY_TODAY:
			cursor = DataHandler.getTaskByDate(this, calendar);
			break;
		case Category.CATEGORY_TOMORROW:
			calendar.add(Calendar.DAY_OF_WEEK, 1);
			cursor = DataHandler.getTaskByDate(this, calendar);
			break;
		case Category.CATEGORY_WEEK:
			break;
		case Category.CATEGORY_ALL:
			cursor = DataHandler.getTaskByStatus(this, Task.TASK_STATUS_ALL);
			break;
		}
		if (cursor == null || cursor.getCount() == 0) {
			finish();
			return;
		}
		displayTask();
	}

	private void displayTask() {
		if (!cursor.moveToPosition(cur_position)) {
			return;
		}
		Task task = new Task(cursor);
		FragmentManager fm = getSupportFragmentManager();
		TaskDetailFrag frag = new TaskDetailFrag(task);
		FragmentTransaction ft = fm.beginTransaction();
		// 添加动画效果
		ft.replace(R.id.taskdetail_frag_container, frag);
		ft.commitAllowingStateLoss();
	}

	private void goToEdit() {
		Intent intent = new Intent(this, NewTaskActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(TaskColumns._ID, cursor.getInt(0));
		extra.putInt(TaskColumns.TASK_STATUS, cursor.getInt(1));
		extra.putString(TaskColumns.TASK_NAME, cursor.getString(2));
		extra.putString(TaskColumns.TASK_DETAIL, cursor.getString(3));
		extra.putInt(TaskColumns.TASK_LEVEL, cursor.getInt(4));
		extra.putInt(TaskColumns.TASK_PLAN_ID, cursor.getInt(5));
		extra.putInt(TaskColumns.TASK_ALARM_ID, cursor.getInt(6));
		extra.putString(TaskColumns.TASK_CREATE_DATE, cursor.getString(7));
		intent.putExtra(CommonVar.TASK_BUNDLE, extra);
		startActivityForResult(intent, CommonVar.TASK_REQUESTCODE_EDIT);
	}

	private void showPopupMenu() {
		final PopupMenu popupMenu = new PopupMenu(this);
		String[] items = getResources().getStringArray(R.array.detail_menu);
		int[] icons = { R.drawable.btn_share, R.drawable.btn_edit,
				R.drawable.btn_delete };
		popupMenu.setWindow(icons, items, R.layout.list_child_popup_menu,
				R.drawable.bg_popwindow_menu, btnMenu.getWidth() * 2,
				LayoutParams.WRAP_CONTENT);
		popupMenu.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:// share
					break;
				case 1:
					goToEdit();
					break;
				case 2:
					deleteTask();
					break;
				default:
					break;
				}
				popupMenu.dismiss();
			}
		});
		popupMenu.showAsDropDown(btnMenu, 0, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 如果更新了闹钟
		if (requestCode == CommonVar.ALARM_REQUESTCODE_SET_ALARM) {
			if (resultCode == CommonVar.ALARM_RESULTCODE_DELETE) {
				Log.e(TAG, "  删除 闹钟的通知");
				deleteTaskAlarm();
			}
		}
		UiUtils.updateMainList(this);
		initData();
	}

	private void deleteTaskAlarm() {
		ContentValues values = new ContentValues();
		values.put(TaskColumns.TASK_ALARM_ID, -1);
		DataHandler.updateTask(this, cursor.getInt(0), values);
	}

	private void deleteTask() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.str_delete);
		builder.setMessage(R.string.str_delete_confirm);
		builder.setPositiveButton(R.string.str_confirm, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int taskId = cursor.getInt(0);
				int taskAlarmId = cursor.getInt(6);
				AlarmHandler.deleteAlarm(TaskDetailActivity.this, taskAlarmId);
				if (DataHandler.delete(TaskDetailActivity.this,
						DBAdapter.TASK_TABLE, taskId)) {
					UiUtils.updateMainList(TaskDetailActivity.this);
					finish();
				}
			}
		});
		builder.setNegativeButton(R.string.str_cancel, null);
		builder.show();
	}

	private void displayPreviousTask() {
		// 向前滚时不能超过cursor的范围 0<pos<cursor.getCount-1
		if (cur_position > 0 && cur_position < cursor.getCount()) {
			--cur_position;
			displayTask();
		} else {
			Toast.makeText(this, "已经是第一条任务", Toast.LENGTH_SHORT).show();
		}
	}

	private void displayNextTask() {
		// 向后滚时，不能超过cursor的范围 -1<pos<cursor.getCount-1
		if (cur_position > -1 && cur_position < cursor.getCount() - 1) {
			++cur_position;
			displayTask();
		} else {
			Toast.makeText(this, "已经到最后一条任务", Toast.LENGTH_SHORT).show();
		}
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnMenu) {
			showPopupMenu();
		} else if (v == btnPrev) {
			displayPreviousTask();
		} else if (v == btnNext) {
			displayNextTask();
		}
	}
}
