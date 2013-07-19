package sql;

import helper.GeneralHelper;

import java.util.ArrayList;

import model.Manga;
import sql.MangaListTable.MangaList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


public class SaveDatabaseTask extends AsyncTask<String, Void, String> {
	private SQLiteDatabase db;
	private MangaListAccesser mDbHelper;
	public ArrayList<Manga> mangaList;
	
	public SaveDatabaseTask(MangaListAccesser mDbHelper, ArrayList<Manga> mangaList) {
		this.mDbHelper = mDbHelper;
		this.db = mDbHelper.getWritableDatabase();
		this.mangaList = mangaList;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		mDbHelper.onUpgrade(db,0,0);
		db.beginTransaction();
		for(Manga manga : mangaList) {
			int index = GeneralHelper.getIndex(manga, mangaList);
			manga = saveMangaToDatabase(manga);
			mangaList.set(index, manga);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return null;
	}
	
	public Manga saveMangaToDatabase(Manga manga) {
		ContentValues values = new ContentValues();
		values.put(MangaList.COLUMN_NAME_TITLE, manga.title);
		values.put(MangaList.COLUMN_NAME_AUTHOR, manga.author);
		values.put(MangaList.COLUMN_NAME_URL, manga.url);
		values.put(MangaList.COLUMN_NAME_LAST_CHAPTER, manga.newestChapter);
		values.put(MangaList.COLUMN_NAME_LAST_UPDATED, manga.lastUpdated);
		values.put(MangaList.COLUMN_NAME_IMG_URL, manga.imgUrl);
		values.put(MangaList.COLUMN_NAME_READ_CHAPTER, manga.readChapter);
		values.put(MangaList.COLUMN_NAME_IS_SAVED, manga.isSaved);

		db.insert(MangaList.TABLE_NAME,
				  null,
				  values);
		String query = "SELECT " + MangaList.COLUMN_NAME_MANGA_ID;
		query += " FROM " + MangaList.TABLE_NAME;
		query += " ORDER BY " + MangaList.COLUMN_NAME_MANGA_ID + " DESC limit 1";
		Cursor c = db.rawQuery(query, null);
		int newId = -1;
		if (c != null && c.moveToFirst())
		    newId = c.getInt(0);
		manga.id = newId;
		return manga;
	}
}
