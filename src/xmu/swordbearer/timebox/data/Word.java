package xmu.swordbearer.timebox.data;

import android.provider.BaseColumns;

public class Word {
	public static class WordColumns implements BaseColumns {
		public static final String WORD_DATE = "word_date";
		public static final String WORD_CONTENT = "word_content";
		public static final String WORD_COLOR = "word_color";
		public static final String[] WORD_DEFAULT_COLUMNS = { _ID, WORD_DATE,
				WORD_CONTENT, WORD_COLOR };
	}

	public int id;
	public String date;
	public String content;
	public int color;

	// public Word() {
	// id = -1;
	// date = CalendarUtil.calendar2LongString(Calendar.getInstance());
	// content = "";
	// }
}
