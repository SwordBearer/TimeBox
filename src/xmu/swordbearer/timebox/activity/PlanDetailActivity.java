package xmu.swordbearer.timebox.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Plan;
import xmu.swordbearer.timebox.data.Plan.PlanColumns;
import xmu.swordbearer.timebox.data.PlanTaskListAdapter;
import xmu.swordbearer.timebox.data.Task;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import xmu.swordbearer.timebox.fragment.TaskDetailDialog;
import xmu.swordbearer.timebox.utils.UiUtils;
import xmu.swordbearer.timebox.view.PopupMenu;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class PlanDetailActivity extends FragmentActivity implements PlanTaskListAdapter.OnItemChangedListener, OnClickListener {
	String TAG = "PlanDetail";

	private TextView nameTextView;
	private TextView detailTextView;
	private TextView createTimeTextView;
	private ImageButton btnBack;
	private ImageButton btnMenu;
	private Button btnAddTask;
	private ListView taskListView;
	private PlanTaskListAdapter taskListAdapter;
	private Plan plan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_plan_detail);
		initViews();
		initData();
	}

	private void initViews() {
		btnBack = (ImageButton) findViewById(R.id.plandetail_btn_back);
		btnMenu = (ImageButton) findViewById(R.id.plandetail_btn_menu);
		btnAddTask = (Button) findViewById(R.id.plandetail_add_task);
		taskListView = (ListView) findViewById(R.id.plandetail_task_listview);
		nameTextView = (TextView) findViewById(R.id.plandetail_plan_name);
		detailTextView = (TextView) findViewById(R.id.plandetail_detail);
		createTimeTextView = (TextView) findViewById(R.id.plandetail_createtime);
		//
		btnBack.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
		btnAddTask.setOnClickListener(this);
		taskListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showTaskDetail(position);
			}
		});
	}

	private void initData() {
		Intent intent = this.getIntent();
		int planId = intent.getIntExtra(PlanColumns._ID, -1);
		if (planId == -1) {
			Toast.makeText(this, "计划查询失败!!!", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		Cursor cursor = DataHandler.queryById(this, DBAdapter.PLAN_TABLE, planId);
		plan = new Plan(cursor);
		nameTextView.setText(plan.name);
		createTimeTextView.setText(plan.startTime);
		if (plan.detail.length() == 0) {
			findViewById(R.id.plandetail_detail_panel).setVisibility(View.GONE);
		} else {
			detailTextView.setText(plan.detail);
		}
		updateTaskList();
	}

	private void showPopupMenu() {
		final PopupMenu popupMenu = new PopupMenu(this);
		String[] items = getResources().getStringArray(R.array.detail_menu);
		int[] icons = { R.drawable.btn_share, R.drawable.btn_edit, R.drawable.btn_delete };
		popupMenu.setWindow(icons, items, R.layout.list_child_popup_menu, R.drawable.bg_popwindow_menu, btnMenu.getWidth() * 2,
				LayoutParams.WRAP_CONTENT);
		popupMenu.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				switch (position) {
				case 0:// share
					break;
				case 1:
					goToEdit();
					break;
				case 2:
					deletePlan();
					break;
				default:
					break;
				}
				popupMenu.dismiss();
			}
		});
		popupMenu.showAsDropDown(btnMenu, 0, 0);
	}

	private void updateTaskList() {
		DBAdapter dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		Cursor taskCursor = DataHandler.getPlanTask(dbAdapter, plan.id);
		dbAdapter.close();
		taskListAdapter = new PlanTaskListAdapter(this, taskCursor);
		taskListView.setAdapter(taskListAdapter);
	}

	private void showTaskDetail(int position) {
		Cursor cursor = taskListAdapter.getCursor();
		if (!cursor.moveToPosition(position)) {
			return;
		}
		TaskDetailDialog tdd = new TaskDetailDialog(new Task(cursor));
		tdd.setShowsDialog(true);
		tdd.show(getSupportFragmentManager(), "任务详情");
	}

	private void showTaskList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final ListView lv = new ListView(this);
		// lv.setBackgroundResource(R.drawable.bg_popup_listview);
		Cursor cursor = DataHandler.getTaskByStatus(this, Task.TASK_STATUS_ALL);
		int count = cursor.getCount();
		if (count == 0) {
			Toast.makeText(this, "暂时没有任务", Toast.LENGTH_SHORT).show();
			return;
		}
		final ArrayList<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>(count);
		HashMap<String, Object> map = null;
		if (cursor.moveToFirst()) {
			do {// 这样写防止第一条被跳过
				map = new HashMap<String, Object>();
				map.put(TaskColumns._ID, cursor.getInt(0));
				map.put(TaskColumns.TASK_NAME, cursor.getString(2));
				taskList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		SimpleAdapter adapter = new SimpleAdapter(this, taskList, android.R.layout.simple_list_item_multiple_choice,
				new String[] { TaskColumns.TASK_NAME }, new int[] { android.R.id.text1 });
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		lv.setAdapter(adapter);

		builder.setTitle("选择任务");
		builder.setView(lv);
		builder.setPositiveButton(R.string.str_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				long[] ids = lv.getCheckItemIds();
				ContentValues values = new ContentValues();
				values.put(TaskColumns.TASK_PLAN_ID, plan.id);
				for (int i = 0; i < ids.length; i++) {
					int selectedTaskId = (Integer) taskList.get((int) ids[i]).get(TaskColumns._ID);
					DataHandler.updateTask(PlanDetailActivity.this, selectedTaskId, values);
				}
				updateTaskList();
			}
		});

		builder.setNegativeButton(R.string.str_cancel, null);
		builder.show();
	}

	private void deletePlan() {
		AlertDialog.Builder builder = new AlertDialog.Builder(PlanDetailActivity.this);
		builder.setTitle(R.string.str_delete_plan);
		builder.setMessage(R.string.str_delete_plan_msg);
		builder.setPositiveButton(R.string.str_confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				DBAdapter dbAdapter = new DBAdapter(PlanDetailActivity.this);
				dbAdapter.open();
				if (!dbAdapter.delete(DBAdapter.PLAN_TABLE, plan.id)) {
					Toast.makeText(PlanDetailActivity.this, "计划删除失败!!!", Toast.LENGTH_LONG).show();
					return;
				}
				Cursor taskCursor = DataHandler.getPlanTask(dbAdapter, plan.id);
				ContentValues values = new ContentValues();
				values.put(TaskColumns.TASK_PLAN_ID, -1);
				DataHandler.updateTasks(dbAdapter, taskCursor, values);
				dbAdapter.close();
				UiUtils.updateMainList(PlanDetailActivity.this);
				finish();

			}
		});
		builder.setNegativeButton(R.string.str_cancel, null);
		builder.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CommonVar.PLAN_REQUESTCODE_EDIT) {
			if (resultCode == CommonVar.PLAN_RESULTCODE_OK) {
				initData();
			}
		}
	}

	private void goToEdit() {
		Intent intent = new Intent(this, NewPlanActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(PlanColumns._ID, plan.id);
		extra.putString(PlanColumns.PLAN_NAME, plan.name);
		extra.putString(PlanColumns.PLAN_DETAIL, plan.detail);
		extra.putString(PlanColumns.PLAN_START, plan.startTime);
		intent.putExtra(CommonVar.PLAN_BUNDLE, extra);
		startActivityForResult(intent, CommonVar.PLAN_REQUESTCODE_EDIT);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onItemChanged() {
		updateTaskList();
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnMenu) {
			showPopupMenu();
		} else if (v == btnAddTask) {
			showTaskList();
		}
	}

}
