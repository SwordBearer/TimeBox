package xmu.swordbearer.timebox.activity;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.Plan;
import xmu.swordbearer.timebox.data.Plan.PlanColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class NewPlanActivity extends Activity implements OnClickListener {
	private EditText nameEditText;
	private EditText detailEditText;
	private ImageButton btnBack;
	private ImageButton btnSave;
	private Button btnStart;
	private TextView dateTextView;

	private int mId = -1;
	private String startTime;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_new_plan);
		initView();
		initData();
	}

	private void initView() {
		dateTextView = (TextView) findViewById(R.id.newplan_tv_date);
		nameEditText = (EditText) findViewById(R.id.newplan_name);
		detailEditText = (EditText) findViewById(R.id.newplan_detail);
		btnBack = (ImageButton) findViewById(R.id.newplan_btn_back);
		btnStart = (Button) findViewById(R.id.newplan_btn_start);
		btnSave = (ImageButton) findViewById(R.id.newplan_btn_save);
		//

		Calendar calendar = Calendar.getInstance();
		dateTextView.setText(CalendarUtil.calendar2TimeString(calendar) + " "
				+ CalendarUtil.getWeekDay(calendar));
		startTime = CalendarUtil.calendar2LongString(Calendar.getInstance());
		btnStart.setText(getResources().getString(R.string.str_start_time)
				+ startTime.substring(0, 16));

		btnBack.setOnClickListener(this);
		btnStart.setOnClickListener(this);
		btnSave.setOnClickListener(this);
	}

	private void initData() {
		Intent intent = this.getIntent();
		Bundle extra = intent.getBundleExtra(CommonVar.PLAN_BUNDLE);
		if (extra != null) {// 修改
			mId = extra.getInt(PlanColumns._ID, -1);
			if (mId == -1) {
				finish();
				return;
			}
			nameEditText.setText(extra.getString(PlanColumns.PLAN_NAME));
			detailEditText.setText(extra.getString(PlanColumns.PLAN_DETAIL));
			btnStart.setText(extra.getString(PlanColumns.PLAN_START));
		}
	}

	private boolean savePlan() {
		String name = nameEditText.getText().toString().trim();
		if (name.length() < 1 || name.length() > 20) {
			Toast.makeText(this, R.string.str_planname_toolong,
					Toast.LENGTH_LONG).show();
			nameEditText.requestFocus();
			return false;
		}
		String detail = detailEditText.getText().toString().trim();
		if (detail.length() > 300) {
			Toast.makeText(this, R.string.str_plandetail_toolong,
					Toast.LENGTH_LONG).show();
			detailEditText.requestFocus();
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(PlanColumns.PLAN_NAME, name);
		values.put(PlanColumns.PLAN_DETAIL, detail);
		values.put(PlanColumns.PLAN_START, startTime);
		values.put(PlanColumns.PLAN_STATUS, Plan.PLAN_STATUS_UNFINISHED);

		DBAdapter dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		boolean ret = false;
		if (mId == -1) {// 新建
			ret = dbAdapter.insert(DBAdapter.PLAN_TABLE, values);
		} else {
			ret = dbAdapter.update(DBAdapter.PLAN_TABLE, mId, values);
		}
		dbAdapter.close();
		if (!ret) {
			Toast.makeText(this, "计划新建失败", Toast.LENGTH_LONG).show();
		}
		return ret;
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnSave) {
			if (savePlan()) {
				setResult(CommonVar.PLAN_RESULTCODE_OK);
				UiUtils.updateMainList(NewPlanActivity.this);
				finish();
			} else {
				Toast.makeText(NewPlanActivity.this,
						R.string.str_save_plan_fail, Toast.LENGTH_LONG).show();
			}
		} else if (v == btnStart) {
			final Calendar c = Calendar.getInstance();
			DatePickerDialog dd = new DatePickerDialog(NewPlanActivity.this,
					new OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Calendar startDate = Calendar.getInstance();
							startDate.set(Calendar.YEAR, year);
							startDate.set(Calendar.MONTH, monthOfYear);
							startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							startTime = CalendarUtil.calendar2LongString(
									startDate).substring(0, 16);
							btnStart.setText(getResources().getString(
									R.string.str_start_time)
									+ startTime);
						}
					}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));
			dd.show();
		}
	}
}
