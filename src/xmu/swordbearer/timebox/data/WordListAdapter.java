package xmu.swordbearer.timebox.data;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WordListAdapter extends BaseCursorAdapter {
	String TAG = "WordListAdapter";
	public int[] modeColors;

	/*
	 * 向左滑动删除，向右边滑动则修改
	 */
	public interface onWordSlideListener {
		public void onSlide(int position);
	}

	public WordListAdapter(Context context, Cursor c) {
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
		int color = cursor.getInt(3);
		if (color == modeColors[0]) {// green
			return inflater.inflate(R.layout.list_child_word_left_green, null,
					false);
		} else if (color == modeColors[5]) {
			return inflater.inflate(R.layout.list_child_word_right_gray, null,
					false);
		}
		return inflater.inflate(R.layout.list_child_word_left_green, parent,
				false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView timeTextView = (TextView) view
				.findViewById(R.id.wordlist_child_date);
		TextView contentTextView = (TextView) view
				.findViewById(R.id.wordlist_child_content);
		timeTextView.setText(cursor.getString(1) + "");// time
		contentTextView.setText(cursor.getString(2));// content
	}
}
