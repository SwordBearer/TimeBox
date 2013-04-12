package xmu.swordbearer.timebox.activity;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DBAdapter;
import xmu.swordbearer.timebox.data.Word.WordColumns;
import xmu.swordbearer.timebox.utils.CalendarUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class NewWordActivity extends Activity implements OnClickListener {
	String TAG = "NewWordActivity";

	private TextView dateTextView;
	private EditText contentEditText;
	private ImageButton btnColor;
	private ImageButton btnBack;
	private ImageButton btnSave;

	private int mId = -1;
	private int color;
	int[] moodColors;
	int[] contentBgs;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_new_word);

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
		dateTextView = (TextView) findViewById(R.id.newword_tv_date);
		contentEditText = (EditText) findViewById(R.id.newword_content);
		btnColor = (ImageButton) findViewById(R.id.newword_color);
		btnBack = (ImageButton) findViewById(R.id.newword_btn_back);
		btnSave = (ImageButton) findViewById(R.id.newword_btn_save);

		Calendar calendar = Calendar.getInstance();
		dateTextView.setText(CalendarUtil.calendar2TimeString(calendar) + " "
				+ CalendarUtil.getWeekDay(calendar));

		btnBack.setOnClickListener(this);
		btnColor.setOnClickListener(this);
		btnSave.setOnClickListener(this);

	}

	private void initData() {
		Intent intent = this.getIntent();
		Bundle extra = intent.getBundleExtra(CommonVar.WORD_BUNDLE);
		if (extra != null) {
			mId = extra.getInt(WordColumns._ID, -1);
			if (mId == -1) {
				finish();
				return;
			}
			color = extra.getInt(WordColumns.WORD_COLOR, moodColors[0]);
			contentEditText.setText(extra.getString(WordColumns.WORD_CONTENT));
		}
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
					contentEditText.setBackgroundResource(contentBgs[j]);
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

	private boolean saveWord() {
		String content = contentEditText.getText().toString().trim();
		if (content.length() > 300 || content.length() == 0) {
			Toast.makeText(NewWordActivity.this, R.string.str_word_toolong,
					Toast.LENGTH_LONG).show();
			warnContnet();
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(WordColumns.WORD_CONTENT, content);
		values.put(WordColumns.WORD_COLOR, color);

		DBAdapter dbAdapter = new DBAdapter(this);
		dbAdapter.open();

		boolean ret = false;
		if (mId == -1) {
			values.put(WordColumns.WORD_DATE,
					CalendarUtil.calendar2LongString(Calendar.getInstance()));
			ret = dbAdapter.insert(DBAdapter.WORD_TABLE, values);
		} else {
			ret = dbAdapter.update(DBAdapter.WORD_TABLE, mId, values);
		}
		if (!ret) {
			Toast.makeText(this, "语录保存失败", Toast.LENGTH_SHORT).show();
		}
		dbAdapter.close();
		return ret;
	}

	private void warnContnet() {
		runOnUiThread(new Runnable() {
			public void run() {
				contentEditText.setBackgroundColor(Color.RED);
				// 闪烁提醒
			}
		});
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnColor) {
			showMoodWindow();
		} else if (v == btnSave) {
			if (saveWord()) {
				UiUtils.updateMainList(NewWordActivity.this);
				finish();
			}
		}
	}
}
