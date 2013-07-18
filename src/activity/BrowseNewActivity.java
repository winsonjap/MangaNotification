package activity;

import helper.GeneralHelper;
import helper.OnlineParseOne;
import helper.OnlineParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import model.AZAdapter;
import model.FunctorsInterface;
import model.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sql.MangaListAccesser;
import sql.MangaListTable.MangaList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.example.manganotification.R;

@SuppressLint("NewApi")
public class BrowseNewActivity extends Activity {
	private ListView listView;
	private ArrayList<Manga> mangaList;
	private EditText inputSearch;
	private AZAdapter<String> azadapter;
	private MangaListAccesser mDbHelper;
	private Context currContext;

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currContext = this;
		this.mDbHelper = new MangaListAccesser(this);
		
		//mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(),0,0);
		
		mangaList = new ArrayList<Manga>();
		mangaList = getFromDatabase();
		setContentView(R.layout.activity_browse_new);
		handleInputSearch();
		listView = (ListView) findViewById(R.id.mangaListView);
		listView.setFastScrollEnabled(true);
		azadapter = new AZAdapter<String>(
				this, android.R.layout.simple_list_item_1,
				mangaList);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				Manga manga = (Manga) azadapter.mangaList.get(position);
				if(manga.title!="") {
					class SaveAction implements FunctorsInterface {
						private Manga manga;
						public SaveAction(Manga manga) {
							this.manga = manga;
						}
						
						@Override
						public void positiveAction() {
							if(manga.isSaved==1) {
								int index = getIndex(manga);
								manga.isSaved = 0;
								manga.newestChapter = "";
								manga.lastUpdated = "";
								manga.readChapter = "";
								setSaved(manga, 0);
								mangaList.set(index, manga);
								azadapter.clear();
								azadapter.addAll(mangaList);
								azadapter.origMangaList = mangaList;
								azadapter.notifyDataSetChanged();
								azadapter.setIndexer();
							}
							else {
								refreshMangaDetail(this.manga);
							}
						}
						@Override
						public void negativeAction() {
							System.out.println("CANCEL");
						}
					}
					String message = "Save this Manga?";
					String save = getString(R.string.save_string);
					if(manga.isSaved==1) {
						message = "Unfollowing this Manga?";
						save = "UnSave!!";
					}
//					message += manga.isSaved + " - ";
//					message += manga.newestChapter + " - " + manga.readChapter + " -";
					AlertDialog.Builder dialog = GeneralHelper.buildDialog(message, save, 
							getString(R.string.cancel_string), currContext, new SaveAction(manga), true);
					dialog.show();
				}
			} 
		});
		listView.setAdapter(azadapter);
	}

	private ArrayList<Manga> getFromDatabase() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		ArrayList<Manga> retList = new ArrayList<Manga>();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		String[] projection = {
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
			retList.add(GeneralHelper.getData(c));
			position++;
		}
		return retList;
	}
	
	private void setSaved(Manga manga, int newVal) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String title = manga.title;
		String url = manga.url;
		// New value for one column
		ContentValues values = new ContentValues();
		values.put(MangaList.COLUMN_NAME_IS_SAVED, newVal);
		if(GeneralHelper.isEmptyStringChecker(manga.readChapter))
			values.put(MangaList.COLUMN_NAME_READ_CHAPTER, "0");

		// Which row to update, based on the ID
		String selection = MangaList.COLUMN_NAME_TITLE + " =?";
		selection += " and ";
		selection += MangaList.COLUMN_NAME_URL + " =?";
		String[] selectionArgs = { title, url };

		db.update(
		    MangaList.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
	}
	
	private void refreshMangaDetail(Manga manga) {
		try {
			int index = getIndex(manga);
			manga = this.parseDetailContent(manga);
			manga.isSaved = 1;
			mangaList.set(index,manga);
			setSaved(manga,1);
			azadapter.clear();
			azadapter.addAll(mangaList);
			azadapter.origMangaList = mangaList;
			azadapter.notifyDataSetChanged();
			azadapter.setIndexer();
		} catch(IllegalArgumentException e) {
			System.out.println("Error getting manga detail");
		}
	}
	
	private int getIndex(Manga checkedManga) throws IllegalArgumentException{
		int retval = 0;
		for(Manga manga : mangaList) {
			if(manga.url.equals(checkedManga.url))
				return retval;
			retval++;
		}
		throw new IllegalArgumentException();
	}

	private Manga parseDetailContent(Manga manga) {
		OnlineParseOne oneParser = new OnlineParseOne(mDbHelper, manga);
		try {
			oneParser.execute("").get();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return oneParser.manga;
	}

	@SuppressWarnings("unchecked")
	private void refreshMangaList() {
		mangaList = this.parseMangaOnline();
		Collections.sort(mangaList);
		this.saveListToDatabase(mangaList);
		azadapter.clear();
		azadapter.addAll(mangaList);
		azadapter.notifyDataSetChanged();
		azadapter.setIndexer();
	}

	private void saveListToDatabase(ArrayList<Manga> mangaList) {
		SaveDatabaseTask saveTask = new SaveDatabaseTask();
		try {
			saveTask.execute("").get();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void handleInputSearch() {
		inputSearch = (EditText) findViewById(R.id.inputBox);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				BrowseNewActivity.this.azadapter.getFilter().filter(cs);
			}
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {}
		});
	}
	
	private class SaveDatabaseTask extends AsyncTask<String, Void, String> {
		private SQLiteDatabase db;
		
		public SaveDatabaseTask() {
			this.db = mDbHelper.getWritableDatabase();
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			mDbHelper.onUpgrade(db,0,0);
			db.beginTransaction();
			for(Manga manga : mangaList) {
				saveMangaToDatabase(manga);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			return null;
		}
		
		public void saveMangaToDatabase(Manga manga) {
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
		}
	}

	private ArrayList<Manga> parseMangaOnline() {
		OnlineParser parserTask = new OnlineParser(mDbHelper, this);
		try {
			parserTask.execute("").get();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return parserTask.mangaListParsed;
	}

	public void refreshList(View view) {
		String message = "This will refresh the list \n" +
				 "It may take a minute or so depending on your internet connection";
		class RefreshAction implements FunctorsInterface {
			@Override
			public void positiveAction() {
				refreshMangaList();
			}
			@Override
			public void negativeAction() {
				System.out.println("CANCEL");
			}
		}
		AlertDialog.Builder dialog = GeneralHelper.buildDialog(message, getString(R.string.continue_string), 
				getString(R.string.cancel_string), this, new RefreshAction(), true);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse_new, menu);
		return true;
	}
}