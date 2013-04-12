package xmu.swordbearer.timebox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.view.View;

public class ScreenshotUtil {
	static String TAG = "ScreenShotUtil";

	public static Bitmap captureContent(View webView) {
		Paint sAlphaPaint = new Paint();
		sAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		sAlphaPaint.setColor(Color.TRANSPARENT);

		if (webView == null)
			return null;
		Bitmap mCapture = null;
		mCapture = Bitmap.createBitmap(webView.getMeasuredWidth(),
				webView.getMeasuredHeight(), Bitmap.Config.RGB_565);
		mCapture.eraseColor(Color.WHITE);
		Canvas c = new Canvas(mCapture);
		final int left = webView.getScrollX();
		final int top = webView.getScrollY();
		int state = c.save();
		c.translate(-left, -top);
		float scale = 1;
		c.scale(scale, scale, left, top);
		webView.draw(c);
		c.restoreToCount(state);
		// manually anti-alias the edges for the tilt
		c.drawRect(0, 0, 1, mCapture.getHeight(), sAlphaPaint);
		c.drawRect(mCapture.getWidth() - 1, 0, mCapture.getWidth(),
				mCapture.getHeight(), sAlphaPaint);
		c.drawRect(0, 0, mCapture.getWidth(), 1, sAlphaPaint);
		c.drawRect(0, mCapture.getHeight() - 1, mCapture.getWidth(),
				mCapture.getHeight(), sAlphaPaint);
		return mCapture;
	}

	public static void saveBitmap(Bitmap bmp) {
		String SDCARD = android.os.Environment.getExternalStorageDirectory()
				.toString();
		Log.e(TAG, "SDCARD路径  " + SDCARD);
		File file = new File(SDCARD + "/screenshots"
				+ System.currentTimeMillis() + ".jpg");
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
				Log.e(TAG, "保存的截图路径为  " + file.getAbsolutePath());
			}
			fos = new FileOutputStream(file);
			bmp.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "截图保存失败 ");
		}
		Log.e(TAG, "截图保存成功 " + bmp.getWidth() + ":" + bmp.getHeight());
	}
}
