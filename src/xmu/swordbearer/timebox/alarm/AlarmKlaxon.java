package xmu.swordbearer.timebox.alarm;

import java.io.IOException;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.data.CommonVar;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class AlarmKlaxon extends Service {
	private boolean isPlaying = false;
	private Vibrator vibrator;
	private MediaPlayer mediaPlayer;
	private Alarm currentAlarm;
	private AudioManager audioManager;
	private TelephonyManager telephonyManager;

	private static final int KILL = 0x01;
	private static final int FOCUSCHANGE = 0x02;

	private static final float IN_CALL_VOLUME = 0.125f;
	private int initialCallState;
	private boolean currentStates = true;

	private static final int ALARM_TIMEOUT_SECONDS = 10 * 60;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case KILL:
				sendKillBroadcast((Alarm) msg.obj);
				stopSelf();
				break;
			case FOCUSCHANGE:
				switch (msg.arg1) {
				case AudioManager.AUDIOFOCUS_LOSS:
					if (!isPlaying && mediaPlayer != null) {
						stop();
					}
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					if (!isPlaying && mediaPlayer != null) {
						mediaPlayer.pause();
						currentStates = false;
					}
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					if (isPlaying && !currentStates) {
						play(currentAlarm);
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	};

	private PhoneStateListener phoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (state != TelephonyManager.CALL_STATE_IDLE
					&& state != initialCallState) {
				sendKillBroadcast(currentAlarm);
				stopSelf();
			}
		}

	};

	private OnAudioFocusChangeListener onAudioFocusChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			handler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
		}
	};

	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
		super.onDestroy();
		// AlarmHandler.enableAlarm(this, currentAlarm.id, false);
		// Log.e("TEST", "取消了闹钟，设置为 diable");
		stop();
		telephonyManager.listen(phoneStateListener, 0);
		// releaseCpuLock();
		audioManager.abandonAudioFocus(onAudioFocusChangeListener);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			stopSelf();
			return Service.START_NOT_STICKY;
		}

		Alarm alarm = intent.getParcelableExtra(CommonVar.ALARM_INTENT_EXTRA);
		if (alarm == null) {
			stopSelf();
			return Service.START_NOT_STICKY;
		}

		if (currentAlarm != null) {
			sendKillBroadcast(currentAlarm);
		}
		play(alarm);
		currentAlarm = alarm;

		initialCallState = telephonyManager.getCallState();

		return Service.START_STICKY;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// wakelock
	}

	private void sendKillBroadcast(Alarm alarm) {
		long millis = System.currentTimeMillis();
		int minutes = (int) Math.round(millis / 60000.0);
		Intent alarmKilled = new Intent(CommonVar.ALARM_KILLED);
		alarmKilled.putExtra(CommonVar.ALARM_INTENT_EXTRA, alarm);
		alarmKilled.putExtra(CommonVar.ALARM_KILLED_TIMEOUT, minutes);
		sendBroadcast(alarmKilled);
	}

	@SuppressLint("NewApi")
	private void play(Alarm alarm) {
		audioManager.requestAudioFocus(onAudioFocusChangeListener,
				AudioManager.STREAM_ALARM,
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		stop();// 检查是否已经播放

		if (!alarm.silent) {
			Uri alertUri = alarm.alert;
			if (alertUri == null) {
				alertUri = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}

			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mp.stop();
					mp.release();
					mediaPlayer = null;
					return true;
				}
			});

			try {
				//
				if (telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
					mediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
					setDataSourceFromResource(getResources(), mediaPlayer,
							R.raw.in_call_alarm);
				} else {
					mediaPlayer.setDataSource(this, alertUri);
				}
				startAlarm(mediaPlayer);
			} catch (Exception exception) {
				try {
					mediaPlayer.reset();
					setDataSourceFromResource(getResources(), mediaPlayer,
							R.raw.fallbackring);
					startAlarm(mediaPlayer);
				} catch (Exception ex) {
				}
			}
		}

		if (alarm.vibrate) {
			vibrator.vibrate(new long[] { 500, 500 }, 0);
		} else {
			vibrator.cancel();
		}
		enableKiller(alarm);
		isPlaying = true;
	}

	private void startAlarm(MediaPlayer player) throws IllegalStateException,
			IOException {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
		}
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws IllegalArgumentException,
			IllegalStateException, IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}

	public void stop() {
		if (isPlaying) {
			isPlaying = false;
			Intent alarmDone = new Intent(CommonVar.ALARM_DONE_ACTION);
			sendBroadcast(alarmDone);
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			}
			vibrator.cancel();
		}
		disableKiller();
	}

	private void enableKiller(Alarm alarm) {
		handler.sendMessageDelayed(handler.obtainMessage(KILL, alarm),
				1000 * ALARM_TIMEOUT_SECONDS);
	}

	private void disableKiller() {
		handler.removeMessages(KILL);
	}
}
