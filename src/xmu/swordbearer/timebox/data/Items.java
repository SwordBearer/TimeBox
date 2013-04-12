package xmu.swordbearer.timebox.data;

import android.provider.BaseColumns;

public class Items {

	public static class RecordColumns implements BaseColumns {
		public static final String RECORD_TIME = "record_time";// 1
		public static final String RECORD_SHARE = "record_share";// 2
		public static final String RECORD_SCORE = "record_score";// 3
		public static final String RECORD_LEVEL = "record_level";// 4
		public static final String[] RECORD_DEFAULT_COLUMNS = { _ID,
				RECORD_TIME, RECORD_SHARE, RECORD_SCORE, RECORD_LEVEL };
	}

	public static class NoteColumns implements BaseColumns {
		public static final String NOTE_DATE = "note_date";// 1
		public static final String NOTE_CONTENT = "note_content";// 2
		public static final String NOTE_COLOR = "note_color";// 3
		public static final String[] NOTE_DEFAULT_COLUMNS = { _ID, NOTE_DATE,
				NOTE_CONTENT, NOTE_COLOR };
	}

	public static class StoptodoColumns implements BaseColumns {
		public static final String STOPTODO_DATE = "stoptodo_date";// 1
		public static final String STOPTODO_DETAIL = "stoptodo_detail";// 2
	}
}
