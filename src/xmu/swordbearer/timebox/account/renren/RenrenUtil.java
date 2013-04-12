package xmu.swordbearer.timebox.account.renren;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import xmu.swordbearer.timebox.activity.MainActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RenrenUtil {
	static String TAG = "RenrenUtil";
	private static final String APP_ID = "208570";
	private static final String API_KEY = "60fa4c0754e445b08fc3246202e48833";
	private static final String SECRET_KEY = "9ebb6ac401f84de799845550ee03031c";

	private static final String KEY_API_KEY = "api_key";
	private static final String KEY_SECRET = "secret";
	private static final String KEY_APP_ID = "appid";
	//
	private static final String USER_AGENT = "Renren_SDK_SwordBearer";
	private static final String LOGIN_URL = "https://graph.renren.com/oauth/token";
	private static final String[] DEFAULT_PERMISSIONS = { "publish_feed",
			"create_album", "photo_upload", "read_user_album", "status_update" };

	/*
	 * Resource Owner Password方式认证
	 */
	public void authorize(final Activity activity, final Bundle params) {
		final String urlStr = LOGIN_URL;
		final ProgressDialog dialog = new ProgressDialog(activity);
		// dialog.setTitle(R.string.str_logining);
		dialog.show();
		new Thread(new Runnable() {
			public void run() {
				String response = openUrl(urlStr, params);
				if (response == null) {
					return;
				}
				try {
					JSONObject jsonObj = new JSONObject(response);
					RenrenPasswordBean bean = new RenrenPasswordBean(jsonObj);
					if (bean.authorizeAccount()) {
						dialog.dismiss();
						Log.e(TAG, "人人帐号登录成功");
						Intent intent = new Intent();
						intent.setClass(activity, MainActivity.class);
						activity.startActivity(intent);
						activity.finish();
					} else {
						Log.e(TAG, "人人帐号登录失败");
						dialog.dismiss();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void logout(Context context) {
	}

	// 连接
	private String openUrl(String urlStr, Bundle params) {
		String response = "";
		try {
			Log.d(TAG, " URL: " + urlStr);
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", USER_AGENT);

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.getOutputStream().write(encodeUrl(params).getBytes("UTF-8"));

			InputStream is = null;
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				is = conn.getInputStream();
			} else {
				is = conn.getErrorStream();
			}
			response = read(is);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return response;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	/**
	 * 将Key-value转换成用&号链接的URL查询参数形式�?
	 * 
	 * @param parameters
	 * @return
	 */
	public String encodeUrl(Bundle parameters) {
		if (parameters == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append("&");
			}
			sb.append(key + "=" + URLEncoder.encode(parameters.getString(key)));
		}
		Log.d(TAG, "encodeURL�?" + sb.toString());
		return sb.toString();
	}

	public void getRenrenFriends() {
	}

}
