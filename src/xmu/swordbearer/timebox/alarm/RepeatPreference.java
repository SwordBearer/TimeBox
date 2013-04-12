package xmu.swordbearer.timebox.alarm;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class RepeatPreference extends ListPreference {
	// Initial value that can be set with the values saved in the database.
	private Alarm.DaysOfWeek daysOfWeek = new Alarm.DaysOfWeek(0);
	private Alarm.DaysOfWeek newDaysOfWeek = new Alarm.DaysOfWeek(0);

	public RepeatPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		String[] weekdays = new DateFormatSymbols().getWeekdays();
		String[] values = new String[] { weekdays[Calendar.MONDAY],
				weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
				weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
				weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY] };

		setEntries(values);
		setEntryValues(values);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			daysOfWeek.set(newDaysOfWeek);
			setSummary(daysOfWeek.toString(getContext(), true));
			callChangeListener(daysOfWeek);
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		builder.setMultiChoiceItems(entries, daysOfWeek.getBooleanArray(),
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						Log.e("RepeatPref", "选择的星期为 " + which);
						newDaysOfWeek.set(which, isChecked);
						Log.e("RepeatPref",
								"当前的重复为 "
										+ newDaysOfWeek.getCoded()
										+ "  "
										+ newDaysOfWeek.toString(getContext(),
												true));
					}
				});
	}

	public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
		daysOfWeek.set(dow);
		newDaysOfWeek.set(dow);
		setSummary(dow.toString(getContext(), true));
	}

	public Alarm.DaysOfWeek getDaysOfWeek() {
		return daysOfWeek;
	}
}
