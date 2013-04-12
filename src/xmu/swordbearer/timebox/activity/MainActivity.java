package xmu.swordbearer.timebox.activity;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.fragment.CategoryFrag;
import xmu.swordbearer.timebox.fragment.CategoryFrag.Category;
import xmu.swordbearer.timebox.fragment.CategoryFrag.OnCategoryClickedListener;
import xmu.swordbearer.timebox.fragment.DataListFrag;
import xmu.swordbearer.timebox.utils.ScoreUtil;
import xmu.swordbearer.timebox.utils.UiUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class MainActivity extends FragmentActivity implements
		OnCategoryClickedListener, OnClickListener {
	String TAG = "Main";
	private long firstTime = 0;

	private int currentCategory = Category.CATEGORY_TODAY;
	private DataListFrag listFragment;
	private ImageButton btnHideCate;
	private TextView tvTitle;
	private ImageButton btnAdd;
	private ImageButton btnSetting;

	private UpdateListReceiver receiver;
	private String[] titles = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		registerUpdateReceiver();
		FragmentManager fm = getSupportFragmentManager();
		listFragment = (DataListFrag) fm.findFragmentById(R.id.frag_datalist);
		// youmeng
		MobclickAgent.onError(this);
		// UMFeedbackService.enableNewReplyNotification(this,
		// NotificationType.AlertDialog);
	}

	private void registerUpdateReceiver() {
		receiver = new UpdateListReceiver();
		IntentFilter filter = new IntentFilter(CommonVar.ACTION_UPDATE_LIST);
		this.registerReceiver(receiver, filter);
	}

	private void goToNewActivity() {
		if (currentCategory == Category.CATEGORY_PLAN) {// plan
			startActivity(new Intent(this, NewPlanActivity.class));
		} else if (currentCategory == Category.CATEGORY_WORD) {// word
			startActivity(new Intent(this, NewWordActivity.class));
		} else if (currentCategory == Category.CATEGORY_NOTE) {
			startActivity(new Intent(this, NewNoteActivity.class));
		} else if (currentCategory == Category.CATEGORY_STOPTODO) {
			startActivity(new Intent(this, NewStopToDoActivity.class));
		} else {
			startActivity(new Intent(this, NewTaskActivity.class));
		}
	}

	private void initViews() {
		btnHideCate = (ImageButton) findViewById(R.id.head_btn_hide_category);
		tvTitle = (TextView) findViewById(R.id.head_tv_title);
		btnAdd = (ImageButton) findViewById(R.id.head_btn_add);
		btnSetting = (ImageButton) findViewById(R.id.head_btn_setting);

		titles = getResources().getStringArray(R.array.titles);
		updateTitle(Category.CATEGORY_TODAY);
		//
		btnHideCate.setOnClickListener(this);
		btnAdd.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
	}

	private void showHideCategory() {
		FragmentManager fm = getSupportFragmentManager();
		CategoryFrag categoryListFragment = (CategoryFrag) fm
				.findFragmentById(R.id.frag_category);
		if (categoryListFragment == null) {
			return;
		}
		FragmentTransaction ft = fm.beginTransaction();
		if (categoryListFragment.isVisible()) {
			ft.hide(categoryListFragment);
		} else {
			ft.show(categoryListFragment);
		}
		ft.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		UiUtils.showInFullscreen(this);
		MobclickAgent.onResume(this);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long secondTime = System.currentTimeMillis();
			if (secondTime - firstTime > 800) {
				Toast.makeText(MainActivity.this, R.string.str_exit_app,
						Toast.LENGTH_SHORT).show();
				firstTime = secondTime;
				return true;
			} else {
				// 保存数据
				ScoreUtil.saveData(this);
				Toast.makeText(MainActivity.this, "退出前保存数据...",
						Toast.LENGTH_SHORT).show();
				System.exit(0);
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void updateTitle(int category) {
		tvTitle.setText(titles[category]);
	}

	public void onCategoryClicked(int newCategory) {
		this.currentCategory = newCategory;
		updateTitle(newCategory);
		listFragment.updateCategory(currentCategory);
	}

	public void onClick(View v) {
		if (v == btnSetting) {
			Intent intent = new Intent(MainActivity.this,
					SettingsActivity.class);
			startActivity(intent);
		} else if (v == btnAdd) {
			goToNewActivity();
		} else if (v == btnHideCate) {
			showHideCategory();
		}
	}

	/**
	 * 数据变化后，收到更新通知，更新数据列表
	 * 
	 * @author SwordBearer
	 * 
	 */
	public class UpdateListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "收到刷新通知");
			listFragment.updateCategory(currentCategory);
		}
	}
}
