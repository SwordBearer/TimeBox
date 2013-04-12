package xmu.swordbearer.timebox.activity;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Items.StoptodoColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import xmu.swordbearer.timebox.view.PopupMenu;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class NewStopToDoActivity extends Activity implements OnClickListener {
	private TextView dateTextView;
	private ImageButton btnBack;
	private ImageButton btnSave;
	private ImageButton btnMenu;
	private TextView tvDetail;
	private EditText editTextDetail;

	private boolean isEditable = false;

	private int mId = -1;
	private String mDetail = "";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_stoptodo);
		initViews();
		initData();
	}

	private void initViews() {
		dateTextView = (TextView) findViewById(R.id.newstop_tv_date);
		btnBack = (ImageButton) findViewById(R.id.newstop_btn_back);
		btnSave = (ImageButton) findViewById(R.id.newstop_btn_save);
		btnMenu = (ImageButton) findViewById(R.id.newstop_btn_menu);

		tvDetail = (TextView) findViewById(R.id.newstop_detail_tv);
		editTextDetail = (EditText) findViewById(R.id.newstop_detail_edit);
		//
		Calendar calendar = Calendar.getInstance();
		dateTextView.setText(CalendarUtil.calendar2TimeString(calendar) + " "
				+ CalendarUtil.getWeekDay(calendar));

		btnBack.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
	}

	private void initData() {
		Intent intent = getIntent();
		Bundle extra = intent.getBundleExtra(CommonVar.STOP_BUNDLE);
		if (extra != null) {// 详情
			mId = extra.getInt(StoptodoColumns._ID, -1);
			if (mId == -1) {
				finish();
			}
			mDetail = extra.getString(StoptodoColumns.STOPTODO_DETAIL);
			goToEdit(false);
		} else {// 新建
			goToEdit(true);
		}
	}

	private void goToEdit(boolean flag) {
		this.isEditable = flag;
		if (isEditable) {
			tvDetail.setVisibility(View.GONE);
			editTextDetail.setVisibility(View.VISIBLE);
			editTextDetail.setText(mDetail);
			editTextDetail.setEnabled(true);
			btnSave.setVisibility(View.VISIBLE);
			btnMenu.setVisibility(View.GONE);
		} else {
			editTextDetail.setVisibility(View.GONE);
			tvDetail.setVisibility(View.VISIBLE);
			tvDetail.setText(mDetail);
			btnSave.setVisibility(View.GONE);
			btnMenu.setVisibility(View.VISIBLE);
		}
	}

	private void showMenu() {
		final PopupMenu popMenu = new PopupMenu(this);
		int icons[] = { R.drawable.btn_share, R.drawable.btn_edit,
				R.drawable.btn_delete };
		String items[] = getResources().getStringArray(R.array.detail_menu);
		popMenu.setWindow(icons, items, R.layout.list_child_popup_menu,
				R.drawable.bg_popwindow_menu, btnMenu.getWidth() * 3,
				LayoutParams.WRAP_CONTENT);

		popMenu.update();
		popMenu.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:
					break;
				case 1:
					goToEdit(true);
					break;
				case 2:
					deleteStoptodo();
				}
				popMenu.dismiss();
			}
		});
		popMenu.showAsDropDown(btnMenu, 0, 0);
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnSave) {
			saveStoptodo();
		} else if (v == btnMenu) {
			showMenu();
		}
	}

	private void deleteStoptodo() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_stoptodo_confirm);
		builder.setPositiveButton(R.string.str_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (DataHandler.delete(NewStopToDoActivity.this,
								DBAdapter.STOPTODO_TABLE, mId)) {
							UiUtils.updateMainList(NewStopToDoActivity.this);
							finish();
						}
					}
				});
		builder.setNegativeButton(R.string.str_cancel, null);
		builder.show();
	}

	private void saveStoptodo() {
		mDetail = editTextDetail.getText().toString().trim();
		if (mDetail.equals("") || mDetail == null) {
			UiUtils.showToast(this, "内容不能为空");
			editTextDetail.requestFocus();
			return;
		}
		boolean ret = false;
		ContentValues values = new ContentValues();
		values.put(StoptodoColumns.STOPTODO_DETAIL, mDetail);

		DBAdapter dbAdapter = new DBAdapter(this);
		dbAdapter.open();
		if (mId == -1) {
			values.put(StoptodoColumns.STOPTODO_DATE,
					CalendarUtil.calendar2LongString(Calendar.getInstance()));
			ret = dbAdapter.insert(DBAdapter.STOPTODO_TABLE, values);
		} else {
			ret = dbAdapter.update(DBAdapter.STOPTODO_TABLE, mId, values);
		}
		dbAdapter.close();
		if (!ret) {
			UiUtils.showToast(this, "数据保存失败...");
		} else {
			UiUtils.updateMainList(this);
			finish();
		}
	}
}
