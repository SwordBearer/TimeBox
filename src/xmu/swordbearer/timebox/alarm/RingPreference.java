package xmu.swordbearer.timebox.alarm;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.util.AttributeSet;

public class RingPreference extends RingtonePreference {
	private Uri mRing;
	private boolean mChangeDefault;

	public RingPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSaveRingtone(Uri ringtoneUri) {
		setRing(ringtoneUri);
		if (mChangeDefault) {
			// 更改系统默认的铃声
			Settings.System.putString(getContext().getContentResolver(),
					Settings.System.ALARM_ALERT, ringtoneUri == null ? null
							: ringtoneUri.toString());
		}
	}

	@Override
	protected Uri onRestoreRingtone() {
		if (RingtoneManager.isDefault(mRing)) {
			return RingtoneManager.getActualDefaultRingtoneUri(getContext(),
					RingtoneManager.TYPE_ALARM);
		}
		return mRing;
	}

	public void setRing(Uri ring) {
		mRing = ring;
		if (ring != null) {
			Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ring);
			if (ring != null) {
				if (ringtone != null)
					setSummary(ringtone.getTitle(getContext()));
			}
		} else {
			setSummary("Silent");
		}
	}

	public String getRingString() {
		if (mRing != null) {
			return mRing.toString();
		} else {
			return "silent";
		}
	}

	public void setChangeDefault() {
		mChangeDefault = true;
	}
}
