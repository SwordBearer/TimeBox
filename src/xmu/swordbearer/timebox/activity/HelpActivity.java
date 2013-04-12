package xmu.swordbearer.timebox.activity;

import xmu.swordbearer.timebox.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		WebView webView = (WebView) findViewById(R.id.help_content_webview);
		webView.loadUrl("file:///android_asset/help.html");
		((ImageButton) findViewById(R.id.help_back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						finish();
					}
				});
	}
}
