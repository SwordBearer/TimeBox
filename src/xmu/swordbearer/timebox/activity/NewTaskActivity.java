package xmu.swordbearer.timebox.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.alarm.AlarmHandler;
import xmu.swordbearer.timebox.alarm.SetAlarm;
import xmu.swordbearer.timebox.data.AlarmTimeAdapter;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Plan.PlanColumns;
import xmu.swordbearer.timebox.data.Task;
import xmu.swordbearer.timebox.data.Task.TaskColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import xmu.swordbearer.timebox.utils.ScoreUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import xmu.swordbearer.timebox.view.CustomDialog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class NewTaskActivity extends Activity implements View.OnClickListener,
		OnItemClickListener {
	String TAG = "NewTaskActivity";

	private ImageButton btnBack;
	private ImageButton btnSave;
	private Button btnAddAlarm;
	private EditText editName;
	private EditText editDetail;
	private View levelView;
	private TextView levelTextView;
	private View planView;
	private TextView planTextView;
	private ListView alarmListView;
	private TextView dateTextView;

	private String[] levels;
	private Task task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_new_task);

		initView();
		initData();
	}

	private void initView() {
		btnBack = (ImageButton) findViewById(R.id.newtask_btn_back);
		dateTextView = (TextView) findViewById(R.id.newtask_tv_date);
		editName = (EditText) findViewById(R.id.newtask_name);
		editDetail = (EditText) findViewById(R.id.newtask_detail);
		levelView = (View) findViewById(R.id.newtask_level);
		levels = getResources().getStringArray(R.array.task_level);
		levelTextView = (TextView) levelView
				.findViewById(R.id.newtask_textview_level);
		planView = (View) findViewById(R.id.newtask_plan);
		planTextView = (TextView) findViewById(R.id.newtask_textview_plan);
		alarmListView = (ListView) findViewById(R.id.newtask_alarmlist);
		btnAddAlarm = (Button) findViewById(R.id.newtask_btn_add_alarm);
		btnSave = (ImageButton) findViewById(R.id.newtask_btn_save);
		//

		Calendar calendar = Calendar.getInstance();
		dateTextView.setText(CalendarUtil.calendar2TimeString(calendar) + " "
				+ CalendarUtil.getWeekDay(calendar));
		levelTextView.setText(levels[Task.TASK_LEVEL_GENERAL]);
		//
		btnAddAlarm.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		levelView.setOnClickListener(this);
		planView.setOnClickListener(this);
	}

	// 初始化数据，有可能是修改任务
	private void initData() {
		task = new Task();
		Intent intent = this.getIntent();
		Bundle extra = intent.getBundleExtra(CommonVar.TASK_BUNDLE);
		// 是修改任务
		if (extra != null) {
			task.id = extra.getInt(TaskColumns._ID, -1);
			Log.e(TAG, "当前获得的task 的ID 是" + task.id);
			if (task.id == -1) {
				finish();
				return;
			}
			String tempStr = extra.getString(TaskColumns.TASK_NAME);
			editName.setText(tempStr);
			task.name = tempStr;
			tempStr = extra.getString(TaskColumns.TASK_DETAIL);
			editDetail.setText(tempStr);
			task.detail = tempStr;
			int tempInt = extra.getInt(TaskColumns.TASK_LEVEL, 2);
			levelTextView.setText(levels[tempInt]);
			task.level = tempInt;
			tempInt = extra.getInt(TaskColumns.TASK_STATUS, -1);
			task.status = tempInt;
			//
			tempInt = extra.getInt(TaskColumns.TASK_PLAN_ID, -1);
			task.planId = tempInt;
			if (task.planId != -1) {
				Cursor c = DataHandler.queryById(this, DBAdapter.PLAN_TABLE,
						task.planId);
				planTextView.setText(c.getString(1));
				c.close();
			}
			tempInt = extra.getInt(TaskColumns.TASK_ALARM_ID, -1);
			task.alarmId = tempInt;

			if (task.alarmId != -1) {// 该任务有闹钟
				updateAlarm();
			}
		}
	}

	private void updateAlarm() {
		if (getContentResolver() != null) {
			Cursor alarmCursor = AlarmHandler.getAlarmsCursor(
					getContentResolver(), task.alarmId);
			if (alarmCursor.getCount() > 0) {
				btnAddAlarm.setVisibility(View.GONE);
				alarmListView
						.setAdapter(new AlarmTimeAdapter(this, alarmCursor));
			} else {
				btnAddAlarm.setVisibility(View.VISIBLE);
				task.alarmId = -1;// 没有闹钟，比如删除了闹钟后，就要更新此任务的alarmId
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 如果是来自SetAlarm的结果
		if (requestCode == CommonVar.ALARM_REQUESTCODE_SET_ALARM) {
			// 如果删除了闹钟
			if (resultCode == CommonVar.ALARM_RESULTCODE_DELETE) {
				task.alarmId = -1;
			}
			// 如果取消新建闹钟
			else if (resultCode == CommonVar.ALARM_RESULTCODE_CANCEL) {
			}
			// 如果新建闹钟成功
			else if (resultCode == CommonVar.ALARM_RESULTCODE_OK) {
				int id = data.getIntExtra(TaskColumns.TASK_ALARM_ID, -1);
				Log.e(TAG, "闹钟新建成功 " + id);
				task.alarmId = id;
			}
			updateAlarm();
		}
	}

	private void setLevel() {
		final PopupWindow pp = new PopupWindow(NewTaskActivity.this);
		ListView lv = new ListView(NewTaskActivity.this);
		lv.setAdapter(new ArrayAdapter<String>(NewTaskActivity.this,
				android.R.layout.simple_spinner_dropdown_item, levels));
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				levelTextView.setText(levels[position]);
				task.level = position;
				pp.dismiss();
			}
		});
		pp.setContentView(lv);
		pp.setWidth(levelView.getWidth() / 2);
		pp.setHeight(LayoutParams.WRAP_CONTENT);
		pp.setOutsideTouchable(true);
		pp.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.shape_popupwindow));
		pp.setFocusable(true);
		pp.showAtLocation(levelView, Gravity.CENTER, 0, levelView.getHeight());
	}

	private void showPlanList() {
		final CustomDialog dialog = new CustomDialog(this);
		final ListView lv = new ListView(this);
		Cursor cursor = DataHandler.getAllPlan(this, 0);
		int count = cursor.getCount();
		if (count == 0) {
			Toast.makeText(this, "暂时没有计划", Toast.LENGTH_SHORT).show();
			return;
		}
		final ArrayList<Map<String, Object>> planList = new ArrayList<Map<String, Object>>(
				count);
		HashMap<String, Object> map = null;
		if (cursor.moveToFirst()) {
			do {// 这样写防止第一条被跳过
				map = new HashMap<String, Object>();
				map.put(PlanColumns._ID, cursor.getInt(0));
				map.put(PlanColumns.PLAN_NAME, cursor.getString(1));
				planList.add(map);
			} while (cursor.moveToNext());
		}
		cursor.close();
		SimpleAdapter adapter = new SimpleAdapter(this, planList,
				android.R.layout.simple_list_item_single_choice,
				new String[] { PlanColumns.PLAN_NAME },
				new int[] { android.R.id.text1 });
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setAdapter(adapter);
		dialog.setTitle(R.string.str_choose_plan);
		dialog.setContentView(lv);

		dialog.setPositiveButton(R.string.str_confirm,
				new View.OnClickListener() {
					public void onClick(View view) {
						int checkedPos = lv.getCheckedItemPosition();
						if (checkedPos < 0) {
							dialog.dismiss();
							return;
						}
						HashMap<String, Object> map = (HashMap<String, Object>) planList
								.get(checkedPos);
						int selectedPlanId = (Integer) map.get(PlanColumns._ID);
						String selectedPlanName = (String) map
								.get(PlanColumns.PLAN_NAME);
						task.planId = selectedPlanId;
						planTextView.setText(selectedPlanName);
						dialog.dismiss();
					}
				});
		dialog.setNeutralButton(R.string.str_delete,
				new View.OnClickListener() {
					public void onClick(View v) {
						task.planId = -1;
						planTextView.setText(R.string.str_not_setup);
						dialog.dismiss();
					}
				});
		dialog.setNegativeButton(R.string.str_cancel,
				new View.OnClickListener() {
					public void onClick(View view) {
						dialog.dismiss();
					}
				});
		dialog.show(planView);
	}

	private boolean saveTask() {
		String taskName = editName.getText().toString().trim();
		String taskDetail = editDetail.getText().toString().trim();
		if (taskName.equals("")) {
			Toast.makeText(this, getString(R.string.str_name_empty),
					Toast.LENGTH_SHORT).show();
			editName.requestFocus();
			return false;
		} else if (taskName.length() > 20) {
			Toast.makeText(this, getString(R.string.str_name_toolong),
					Toast.LENGTH_SHORT).show();
			editName.requestFocus();
			return false;
		}
		if (taskDetail.length() > 120) {
			Toast.makeText(this, getString(R.string.str_detail_toolong),
					Toast.LENGTH_SHORT).show();
			editDetail.requestFocus();
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(TaskColumns.TASK_STATUS, task.status);
		values.put(TaskColumns.TASK_NAME, taskName);
		values.put(TaskColumns.TASK_DETAIL, taskDetail);
		values.put(TaskColumns.TASK_LEVEL, task.level);
		values.put(TaskColumns.TASK_PLAN_ID, task.planId);
		values.put(TaskColumns.TASK_ALARM_ID, task.alarmId);

		DBAdapter dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		boolean ret = false;
		if (task.id == -1) {
			values.put(TaskColumns.TASK_CREATE_DATE,
					CalendarUtil.calendar2LongString(Calendar.getInstance()));
			ret = dbAdapter.insert(DBAdapter.TASK_TABLE, values);
		} else {
			ret = dbAdapter.update(DBAdapter.TASK_TABLE, task.id, values);
		}
		dbAdapter.close();
		if (!ret) {
			Toast.makeText(this, "任务新建失败", Toast.LENGTH_LONG).show();
		}
		return ret;
	}

	private void cancelCreateTask() {
		if (task.id == -1) {// 新建的任务
			if (task.alarmId != -1) {// 取消时，如果该任务有闹钟，就要删除闹钟
				AlarmHandler.deleteAlarm(this, task.alarmId);
			}
		}
		// 编辑
		else {
			// 如果此任务的闹钟还在的话，当取消编辑后，就还原该闹钟
			if (task.alarmId != -1) {
			}
		}
		finish();
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

	private void goToAddAlarm() {
		Intent intent = new Intent(this, SetAlarm.class);
		startActivityForResult(intent, CommonVar.ALARM_REQUESTCODE_SET_ALARM);
	}

	public void onClick(View v) {
		if (v == btnSave) {
			if (saveTask()) {
				UiUtils.updateMainList(this);
				// 修改内容后，要更新详情界面
				setResult(RESULT_OK);
				// 增加积分
				ScoreUtil.addScore(3);
				Toast.makeText(this, "积分增加 " + 3, Toast.LENGTH_SHORT).show();
				finish();
			}
		} else if (v == btnBack) {
			cancelCreateTask();
		} else if (v == btnAddAlarm) {
			goToAddAlarm();
		} else if (v == levelView) {
			setLevel();
		} else if (v == planView) {
			showPlanList();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		cancelCreateTask();
		super.onBackPressed();
	}

	public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
		Intent intent = new Intent(this, SetAlarm.class);
		intent.putExtra(CommonVar.ALARM_ID, task.alarmId);
		startActivityForResult(intent, CommonVar.ALARM_REQUESTCODE_SET_ALARM);
	}
}
