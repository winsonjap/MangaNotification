package sql;

import android.provider.BaseColumns;

public final class MangaListTable {
	// To prevent someone from accidentally instantiating the contract class,
	// give it an empty constructor.
	public MangaListTable() {}

	/* Inner class that defines the table contents */
	public static abstract class MangaList implements BaseColumns {
		public static final String TABLE_NAME = "manga_list_online";
		public static final String COLUMN_NAME_MANGA_ID = "manga_id";
		public static final String COLUMN_NAME_TITLE = "manga_title";
		public static final String COLUMN_NAME_AUTHOR = "manga_author";
		public static final String COLUMN_NAME_URL = "manga_url";
		public static final String COLUMN_NAME_LAST_CHAPTER = "manga_last_chapter";
		public static final String COLUMN_NAME_READ_CHAPTER = "manga_read_chapter";
		public static final String COLUMN_NAME_LAST_UPDATED = "manga_last_updated";
		public static final String COLUMN_NAME_IMG_URL = "manga_img_url";
		public static final String COLUMN_NAME_IS_SAVED = "manga_is_saved";

		private static final String TEXT_TYPE = " TEXT";
		private static final String INT_TYPE = " INTEGER";
		private static final String COMMA_SEP = ",";
		public static final String SQL_CREATE_ENTRIES =
				"CREATE TABLE " + MangaList.TABLE_NAME + " (" +
						MangaList.COLUMN_NAME_MANGA_ID + " INTEGER PRIMARY KEY," +
						MangaList.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_URL + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_LAST_CHAPTER + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_READ_CHAPTER + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_LAST_UPDATED + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_IMG_URL + TEXT_TYPE + COMMA_SEP +
						MangaList.COLUMN_NAME_IS_SAVED + INT_TYPE +
						" )";

		public static final String SQL_DELETE_ENTRIES =
				"DROP TABLE IF EXISTS " + MangaList.TABLE_NAME;

	}
}

