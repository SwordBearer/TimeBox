package xmu.swordbearer.timebox.activity;

import xmu.swordbearer.timebox.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.umeng.fb.UMFeedbackService;

public class SettingsActivity extends ListActivity {
	private static final int ITEM_COUNT = 0;
	private static final int ITEM_ANALYSE = 1;
	private static final int ITEM_ACCOUNT = 2;
	private static final int ITEM_UPDATE = 3;
	private static final int ITEM_FULLSCREEN = 4;
	private static final int ITEM_ALARM_STEP = 5;
	private static final int ITEM_POWER_OFF = 6;
	private static final int ITEM_FEEDBACK = 7;
	private static final int ITEM_HELP = 8;
	private static final int ITEM_ABOUT = 9;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		((ImageButton) findViewById(R.id.setup_back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case ITEM_COUNT:
			break;
		case ITEM_ANALYSE:
			break;
		case ITEM_ACCOUNT:
			startActivity(new Intent(SettingsActivity.this,
					AccountManageActivity.class));
			break;
		case ITEM_UPDATE:
			break;
		case ITEM_FULLSCREEN:
			break;
		case ITEM_ALARM_STEP:
			break;
		case ITEM_POWER_OFF:
			break;
		case ITEM_FEEDBACK:
			UMFeedbackService.openUmengFeedbackSDK(this);
			break;
		case ITEM_HELP:
			startActivity(new Intent(this, HelpActivity.class));
			break;
		case ITEM_ABOUT:// account
			break;
		default:
			break;
		}

	}

}
