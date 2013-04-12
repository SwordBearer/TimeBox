package xmu.swordbearer.timebox.activity;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Items.NoteColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import xmu.swordbearer.timebox.view.PopupMenu;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class NewNoteActivity extends Activity implements OnClickListener {
	private ImageButton btnBack;
	private TextView dateTextView;

	private ImageButton btnMenu;
	private ImageButton btnColor;
	private ImageButton btnSave;

	private EditText editText;

	private boolean isEditable = false;
	private int mId = -1;
	private String content = "";
	private String editDate;
	private int color;

	int[] moodColors;
	int[] contentBgs;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_new_note);

		Resources res = getResources();
		moodColors = new int[] { res.getColor(R.color.mode_green),
				res.getColor(R.color.mode_red),
				res.getColor(R.color.mode_yellow),
				res.getColor(R.color.mode_blue),
				res.getColor(R.color.mode_pink),
				res.getColor(R.color.mode_gray) };
		contentBgs = new int[] { R.drawable.shape_edittext_word_green,
				R.drawable.shape_edittext_word_red,
				R.drawable.shape_edittext_word_yellow,
				R.drawable.shape_edittext_word_blue,
				R.drawable.shape_edittext_word_pink,
				R.drawable.shape_edittext_word_gray };
		color = moodColors[0];

		initView();
		initData();
	}

	private void initView() {
		dateTextView = (TextView) findViewById(R.id.newnote_tv_date);
		btnMenu = (ImageButton) findViewById(R.id.newnote_btn_menu);
		btnColor = (ImageButton) findViewById(R.id.newnote_btn_color);
		btnSave = (ImageButton) findViewById(R.id.newnote_btn_save);
		btnBack = (ImageButton) findViewById(R.id.newnote_btn_back);
		editText = (EditText) findViewById(R.id.newnote_content);
		//
		Calendar calendar = Calendar.getInstance();
		dateTextView.setText(CalendarUtil.calendar2TimeString(calendar) + " "
				+ CalendarUtil.getWeekDay(calendar));

		btnColor.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
		btnSave.setOnClickListener(this);
	}

	private void initData() {
		Intent intent = getIntent();
		Bundle extra = intent.getBundleExtra(CommonVar.NOTE_BUNDLE);
		if (extra != null) {// 编辑
			mId = extra.getInt(NoteColumns._ID, -1);
			if (mId == -1) {
				finish();
				return;
			}
			Cursor cursor = DataHandler.queryById(this, DBAdapter.NOTE_TABLE,
					mId);
			editDate = cursor.getString(1);
			content = cursor.getString(2);
			color = cursor.getInt(3);
			editText.setText(content);
			editText.setBackgroundColor(color);
			isEditable = false;
		} else {// 新建
			editDate = CalendarUtil.calendar2LongString(Calendar.getInstance());
			color = moodColors[0];
			isEditable = true;
		}
		convertEditStatus();
	}

	public void onClick(View v) {
		if (v == btnBack) {
			if (saveNote())
				finish();
		} else if (v == btnColor) {
			showMoodWindow();
		} else if (v == btnMenu) {
			showPopupMenu();
		} else if (v == btnSave) {
			if (saveNote()) {
				finish();
			}
		}
	}

	@Override
	protected void onPause() {
		saveNote();
		super.onPause();
	}

	private void showMoodWindow() {
		View contentView = getLayoutInflater().inflate(R.layout.view_word_mood,
				null);
		final PopupWindow moodPop = new PopupWindow(this);
		moodPop.setContentView(contentView);

		View[] moodBtns = { contentView.findViewById(R.id.mood_btn_green),
				contentView.findViewById(R.id.mood_btn_red),
				contentView.findViewById(R.id.mood_btn_yellow),
				contentView.findViewById(R.id.mood_btn_blue),
				contentView.findViewById(R.id.mood_btn_pink),
				contentView.findViewById(R.id.mood_btn_gray) };
		for (int i = 0; i < moodBtns.length; i++) {
			final int j = i;
			btnColor.setBackgroundColor(j);
			moodBtns[i].setBackgroundColor(moodColors[i]);
			moodBtns[i].setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					moodPop.dismiss();
					editText.setBackgroundResource(contentBgs[j]);
					color = moodColors[j];
				}
			});
		}
		moodPop.setWidth(LayoutParams.WRAP_CONTENT);
		moodPop.setHeight(LayoutParams.WRAP_CONTENT);
		moodPop.setFocusable(true);
		moodPop.setOutsideTouchable(true);
		moodPop.showAsDropDown(btnColor);
	}

	private void convertEditStatus() {
		if (isEditable) {
			editText.setEnabled(true);
			btnColor.setVisibility(View.VISIBLE);
			btnMenu.setVisibility(View.GONE);
			btnSave.setVisibility(View.VISIBLE);
			// 自动显示软键盘
			InputMethodManager imm = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(editText, 0);
		} else {
			editText.setEnabled(false);
			btnColor.setVisibility(View.GONE);
			btnMenu.setVisibility(View.VISIBLE);
			btnSave.setVisibility(View.GONE);
		}
	}

	private void showPopupMenu() {
		final PopupMenu popMenu = new PopupMenu(this);
		popMenu.setAnimationStyle(android.R.style.Animation_Dialog);
		int icons[] = { R.drawable.btn_share, R.drawable.btn_edit,
				R.drawable.btn_delete };
		String items[] = getResources().getStringArray(R.array.detail_menu);
		popMenu.setWindow(icons, items, R.layout.list_child_popup_menu,
				R.drawable.bg_popwindow_menu, btnMenu.getWidth() * 2,
				LayoutParams.WRAP_CONTENT);
		popMenu.update();
		popMenu.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (position) {
				case 0:
					break;
				case 1:
					isEditable = true;
					convertEditStatus();
					break;
				case 2:
					deleteNote();
					break;
				default:
					break;
				}
				popMenu.dismiss();
			}
		});
		popMenu.showAsDropDown(btnMenu, 0, 0);
	}

	private boolean saveNote() {
		boolean ret = false;
		if (isEditable) {
			content = editText.getText().toString().trim();
			if (content.equals("")) {
				Toast.makeText(this, "笔记内容不能为空...", Toast.LENGTH_LONG).show();
				return false;
			}
			editDate = CalendarUtil.calendar2LongString(Calendar.getInstance());
			ContentValues values = new ContentValues();
			values.put(NoteColumns.NOTE_DATE, editDate);
			values.put(NoteColumns.NOTE_CONTENT, content);
			values.put(NoteColumns.NOTE_COLOR, color);

			DBAdapter dbAdapter = new DBAdapter(this);
			dbAdapter.open();
			if (mId == -1) {
				ret = dbAdapter.insert(DBAdapter.NOTE_TABLE, values);
			} else {
				ret = dbAdapter.update(DBAdapter.NOTE_TABLE, mId, values);
				Log.e("NewNote", "保存note");
			}
			isEditable = false;
			dbAdapter.close();
		} else {
			ret = true;
		}
		if (!ret) {
			Toast.makeText(this, "笔记保存失败", Toast.LENGTH_LONG).show();
			return false;
		}
		UiUtils.updateMainList(NewNoteActivity.this);
		return true;
	}

	private void deleteNote() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.delete_note_confirm);
		builder.setPositiveButton(R.string.str_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DataHandler.delete(NewNoteActivity.this,
								DBAdapter.NOTE_TABLE, mId);
						UiUtils.updateMainList(NewNoteActivity.this);
						finish();
					}
				});
		builder.setNegativeButton(R.string.str_cancel, null);
		builder.show();
	}
}
