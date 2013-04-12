package xmu.swordbearer.timebox.account.renren;

import org.json.JSONObject;

public class RenrenPasswordBean {
	public static final String KEY_ACCESS_TOKEN = "access_token";
	public static final String KEY_EXPIRES_IN = "expires_in";
	public static final String KEY_REFRESH_TOKEN = "refresh_token";
	public static final String KEY_SCOPE = "scope";

	private String accessToken;
	private String expire;
	private String refreshToken;
	private String scope;

	public RenrenPasswordBean(JSONObject jsonObj) {
		if (jsonObj == null) {
			return;
		}
		this.accessToken = jsonObj.optString(KEY_ACCESS_TOKEN);
		this.expire = jsonObj.optString(KEY_EXPIRES_IN);
		this.refreshToken = jsonObj.optString(KEY_REFRESH_TOKEN);
		this.scope = jsonObj.optString(KEY_SCOPE);
	}

	public boolean authorizeAccount() {
		return true;
	}
}
