package xmu.swordbearer.timebox.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;

public abstract class BaseCursorAdapter extends CursorAdapter {
	protected LayoutInflater inflater;

	public BaseCursorAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}
}
