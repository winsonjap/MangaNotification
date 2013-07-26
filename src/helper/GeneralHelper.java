package helper;

import java.util.ArrayList;
import java.util.HashMap;

import model.FunctorsInterface;
import model.Manga;

import com.example.manganotification.R;

import sql.MangaListAccesser;
import sql.MangaListTable.MangaList;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GeneralHelper {
	public static AlertDialog.Builder buildDialog(String msg, String pos, 
							String neg, Context ctx, final FunctorsInterface functor,
							boolean cancelable) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(msg)
               .setCancelable(cancelable)
               .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   functor.positiveAction();
                   }
               })
               .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   functor.negativeAction();
                   }
               });
        return builder;
    }
	
	public static AlertDialog.Builder buildErrorDialog(String errMsg, Context context) {
		class ConfirmNothing implements FunctorsInterface {
			@Override
			public void positiveAction() {
				System.out.println("ERROR HAPPENS");
			}
			@Override
			public void negativeAction() {
				System.out.println("CANCEL");
			}
		}
		AlertDialog.Builder dialog = GeneralHelper.buildDialog(errMsg, context.getString(R.string.confirm_string), 
				"", context, new ConfirmNothing(), false);
		return dialog;
	}
	
	public static boolean isEmptyStringChecker(String checked) {
		if(checked == null || checked.equals(" ") || checked.equals(""))
			return true;
		return false;
	}
	
	public static String capitalize(String string)
	{
		if (string == null)
			throw new NullPointerException("no string ");
		if(string == "" || string == " ")
			return string;
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
	
	public static Manga getData(Cursor c) {
		int id = c.getInt(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_MANGA_ID));
		String title = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_TITLE));
		String author = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_AUTHOR));
		String url = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_URL));
		String lastChapter = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_LAST_CHAPTER));
		String readChapter = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_READ_CHAPTER));
		String lastUpdated = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_LAST_UPDATED));
		String imgUrl = c.getString(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_IMG_URL));
		int isSaved = c.getInt(c.getColumnIndexOrThrow(MangaList.COLUMN_NAME_IS_SAVED));
		Manga manga = new Manga(id, title, author, lastChapter, readChapter, lastUpdated, url, imgUrl, isSaved);
		return manga;
	}
	
	/**
	 * Function to get all mangas from database to populate listview
	 * @return ArrayList<Manga>
	 */
	public static ArrayList<Manga> getFromDatabase(MangaListAccesser mDbHelper, boolean saveOnly) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		ArrayList<Manga> retList = new ArrayList<Manga>();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = {
				MangaList.COLUMN_NAME_MANGA_ID,
				MangaList.COLUMN_NAME_TITLE,
				MangaList.COLUMN_NAME_AUTHOR,
				MangaList.COLUMN_NAME_URL,
				MangaList.COLUMN_NAME_LAST_CHAPTER,
				MangaList.COLUMN_NAME_READ_CHAPTER,
				MangaList.COLUMN_NAME_LAST_UPDATED,
				MangaList.COLUMN_NAME_IMG_URL,
				MangaList.COLUMN_NAME_IS_SAVED
		};
		// How you want the results sorted in the resulting Cursor
		String sortOrder = MangaList.COLUMN_NAME_TITLE + " ASC";
		Cursor c = db.query(
				MangaList.TABLE_NAME,  // The table to query
				projection,            // The columns to return
				null,           	   // The columns for the WHERE clause
				null,     		       // The values for the WHERE clause
				null,                  // don't group the rows
				null,                  // don't filter by row groups
				sortOrder              // The sort order
				);
		int position = 0;
		while(c.moveToPosition(position)) {
			Manga addedManga = GeneralHelper.getData(c);
			if(saveOnly) {
				if(addedManga.isSaved == 1)
					retList.add(addedManga);
			}
			else
				retList.add(addedManga);

			//System.out.println("ID : " + addedManga.id);
			//System.out.println("TIT : " + addedManga.title);
			position++;
		}
		return retList;
	}
	
	/**
	 * Getting index of given manga in the given manga list
	 * @param checkedManga
	 * @param mangaList
	 * @return int index of the manga
	 * @throws IllegalArgumentException
	 */
	public static int getIndex(Manga checkedManga, ArrayList<Manga> mangaList) throws IllegalArgumentException{
		int retval = 0;
		for(Manga manga : mangaList) {
			if(manga.id == checkedManga.id)
				return retval;
			retval++;
		}
		throw new IllegalArgumentException();
	}

	public static HashMap<String, Manga> convertToHash(
			ArrayList<Manga> mangaList) {
		HashMap<String, Manga> hashedList = new HashMap<String, Manga>();
		for(Manga manga : mangaList) {
			hashedList.put(manga.url, manga);
		}
		return hashedList;
	}
}
