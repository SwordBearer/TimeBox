package xmu.swordbearer.timebox.activity;

import xmu.swordbearer.timebox.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class AccountManageActivity extends Activity implements OnClickListener {
	ImageButton btnBack;
	ImageButton btnSina;
	ImageButton btnRenren;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_account_manage);
		initViews();
	}

	private void initViews() {
		btnBack = (ImageButton) findViewById(R.id.account_manage_back_btn);
		btnSina = (ImageButton) findViewById(R.id.login_by_sina);
		btnRenren = (ImageButton) findViewById(R.id.login_by_renren);

		btnBack.setOnClickListener(this);
		btnSina.setOnClickListener(this);
		btnRenren.setOnClickListener(this);
	}

	/**
	 * 检测已经登陆的帐号，并更新界面
	 */
	private void updateAccounts() {
	}

	private void loginBySina() {
	}

	private void loginByRenren() {
	}

	public void onClick(View v) {
		if (v == btnBack) {
			finish();
		} else if (v == btnSina) {
			loginBySina();
		} else if (v == btnRenren) {
			loginByRenren();
		}
	}

}
