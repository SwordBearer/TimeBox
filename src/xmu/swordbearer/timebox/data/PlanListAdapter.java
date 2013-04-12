package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlanListAdapter extends BaseCursorAdapter {

	public PlanListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.list_child_plan, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView nameTextView = (TextView) view
				.findViewById(R.id.planlist_child_name);
		TextView startTextView = (TextView) view
				.findViewById(R.id.planlist_child_start);
		nameTextView.setText(cursor.getString(1));
		startTextView.setText(cursor.getString(3).substring(0, 16));
	}

}
