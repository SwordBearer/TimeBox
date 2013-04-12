package xmu.swordbearer.timebox.activity;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.account.renren.RenrenUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	String TAG = "LoginActivity";
	private Button btnConfirm;
	private Button btnCancel;
	private EditText etUserName;
	private EditText etPasswd;
	private Handler handler;

	private int loginPort = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		init();
	}

	private void init() {
		handler = new Handler();

		Intent intent = getIntent();
		loginPort = intent.getIntExtra("loginport", -1);
		Log.e(TAG, "loginPort " + loginPort);

		etUserName = (EditText) findViewById(R.id.login_edit_username);
		etPasswd = (EditText) findViewById(R.id.login_edit_password);
		btnConfirm = (Button) findViewById(R.id.login_btn_confirm);
		btnCancel = (Button) findViewById(R.id.login_btn_cancel);
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String userName = etUserName.getText().toString().trim();
				String passwd = etPasswd.getText().toString().trim();
				if (userName.equals("") || passwd.equals("")) {
					Toast.makeText(Login.this, R.string.str_login_empty,
							Toast.LENGTH_LONG).show();
					return;
				}
				Bundle params = new Bundle();
				params.putString("username", userName);
				params.putString("password", passwd);
				login(params);
			}
		});
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void login(Bundle params) {
		switch (loginPort) {
		case 0:
			RenrenUtil renren = new RenrenUtil();
			renren.authorize(Login.this, params);
			break;
		default:
			break;
		}
	}
}
