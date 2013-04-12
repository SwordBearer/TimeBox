package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NoteListAdapter extends BaseCursorAdapter {

	public int[] modeColors;

	public NoteListAdapter(Context context, Cursor c) {
		super(context, c);
		Resources res = context.getResources();
		modeColors = new int[] { res.getColor(R.color.mode_green),
				res.getColor(R.color.mode_red),
				res.getColor(R.color.mode_yellow),
				res.getColor(R.color.mode_blue),
				res.getColor(R.color.mode_pink),
				res.getColor(R.color.mode_gray) };
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.list_child_note, null);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tView = (TextView) view
				.findViewById(R.id.notelist_child_content);
		tView.setText(cursor.getString(2));
		tView.setBackgroundColor(cursor.getInt(3));
	}

}
