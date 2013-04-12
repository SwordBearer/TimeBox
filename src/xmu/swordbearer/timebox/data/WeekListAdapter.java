package xmu.swordbearer.timebox.data;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

public class WeekListAdapter extends BaseCursorAdapter {
	String TAG = "WeekListAdapter";

	public WeekListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

}
