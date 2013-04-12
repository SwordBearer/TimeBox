package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StoptodoListAdapter extends BaseCursorAdapter {

	public StoptodoListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.list_child_stop, null);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tvName = (TextView) view
				.findViewById(R.id.stoplist_child_name);
		TextView tvDate = (TextView) view
				.findViewById(R.id.stoplist_child_date);
		tvDate.setText(cursor.getString(1));
		tvName.setText(cursor.getString(2));
	}
}
