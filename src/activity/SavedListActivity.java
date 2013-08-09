package activity;

import helper.GeneralHelper;
import helper.OnlineParseOne;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import model.AZAdapter;
import model.FunctorsInterface;
import model.Manga;

import sql.MangaListAccesser;
import sql.MangaListTable.MangaList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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
/**
 * The activity class for Browse New Button
 * which is the activity showing list of all manga list parsed online
 * Differentiate between saved and unsaved mangas
 * User can save manga in this page
 * Ability to filter the listview
 * @author winson
 *
 */
public class SavedListActivity extends Activity {
	public ListView listView;
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
		mangaList = GeneralHelper.getFromDatabase(mDbHelper,true);
		Collections.sort(mangaList);
		setContentView(R.layout.activity_saved_list);
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
					class ReadAction implements FunctorsInterface {
						private Manga manga;
						public ReadAction(Manga manga) {
							this.manga = manga;
						}
						
						@Override
						public void positiveAction() {
							readManga(manga);
						}

						@Override
						public void negativeAction() {
							System.out.println("CANCEL");
						}

						@Override
						public void neutralAction() {
							refreshMangaDetail(manga);
						}
					}
					String message = "Action to do:";
					String read = getString(R.string.read_string);
					String refresh = getString(R.string.refresh_string);
					//message += manga.isSaved + " - ";
					//message += manga.newestChapter + " - " + manga.readChapter + " -";
					AlertDialog.Builder dialog = GeneralHelper.buildDialog(message, read, 
							getString(R.string.cancel_string), currContext, new ReadAction(manga), true, refresh);
					dialog.show();
				}
			} 
		});
		listView.setAdapter(azadapter);
	}
	
	/**
	 * Set the given manga's save status to newVal
	 * IT will automatically save the detail of the manga when saved
	 * @param manga - manga that will be saved/unsaved
	 * @param newVal - value to determine whether to save/unsave
	 */
	private void setSaved(Manga manga, int newVal) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put(MangaList.COLUMN_NAME_IS_SAVED, newVal);
		if(newVal==1) {
			if(GeneralHelper.isEmptyStringChecker(manga.readChapter))
				values.put(MangaList.COLUMN_NAME_READ_CHAPTER, "0");
			else
				values.put(MangaList.COLUMN_NAME_READ_CHAPTER, manga.readChapter);
			values.put(MangaList.COLUMN_NAME_LAST_CHAPTER, manga.newestChapter);
			values.put(MangaList.COLUMN_NAME_LAST_UPDATED, manga.lastUpdated);
			values.put(MangaList.COLUMN_NAME_AUTHOR, manga.author);
			values.put(MangaList.COLUMN_NAME_TITLE, manga.title);
			values.put(MangaList.COLUMN_NAME_URL, manga.url);
			values.put(MangaList.COLUMN_NAME_IMG_URL, manga.imgUrl);
		}
		// Which row to update, based on the ID
		db.update(MangaList.TABLE_NAME,values,
				  MangaList.COLUMN_NAME_MANGA_ID+"="+manga.id,null);
	}
	
	/**
	 * Function to unsave a saved manga
	 * @param manga - manga to be unsaved
	 */
	private void readManga(Manga manga) {
		int index = GeneralHelper.getIndex(manga, mangaList);
		manga.readChapter = manga.newestChapter;
		setSaved(manga, 1);
		mangaList.set(index, manga);
		azadapter.origMangaList = mangaList;
		azadapter.setIndexer();
		azadapter.notifyDataSetChanged();							
	}
	
	/**
	 * Refresh a manga, populating it's detail (author,chapter,etc)
	 * Trigger is when manga being saved -> it will also save manga to
	 * database by calling setSave()
	 * @param manga - manga to be populated
	 */
	private void refreshMangaDetail(Manga manga) {
		try {
			int index = GeneralHelper.getIndex(manga, mangaList);
			manga = this.parseDetailContent(manga);
			if(manga == null) {
				String errMsg = "We're having some trouble connecting to the server\n";
				errMsg += "The troublemaker is 'most' probably the internet connection";
				AlertDialog.Builder dialog = GeneralHelper.buildErrorDialog(errMsg,this);
				dialog.show();
				return;
			}
			manga.isSaved = 1;
			Calendar c = Calendar.getInstance();
			String year = Integer.toString(c.get(Calendar.YEAR));
			String month = Integer.toString(c.get(Calendar.MONTH));
			String day = Integer.toString(c.get(Calendar.DATE));
			manga.lastUpdated = year +"/"+month+"/"+day;
			mangaList.set(index,manga);
			setSaved(manga,1);	
			azadapter.origMangaList = mangaList;
			azadapter.setIndexer();
			azadapter.notifyDataSetChanged();
		} catch(IllegalArgumentException e) {
			System.out.println("Error getting manga detail");
		}
	}

	/**
	 * Function to parse detail of a manga
	 * @param manga - manga to be populated with detail
	 * @return manga - parsed with detail
	 */
	private Manga parseDetailContent(Manga manga) {
		OnlineParseOne oneParser = new OnlineParseOne(mDbHelper, manga);
		try {
			oneParser.execute("").get();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return oneParser.manga;
	}

	/**
	 * Refresh the whole list of listview
	 * It will save to the database as well
	 */
	private void refreshMangaList() {
		for(Manga manga : mangaList) {
			refreshMangaDetail(manga);
		}
		azadapter.notifyDataSetChanged();
	}

	/**
	 * The function to handle filter
	 * It will affected every time user type
	 * in the filter search textbox
	 */
	private void handleInputSearch() {
		inputSearch = (EditText) findViewById(R.id.inputBox);
		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				azadapter.getFilter().filter(cs);
				listView.setSelection(0);
			}
			@Override
			public void afterTextChanged(Editable arg0) {}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {}
		});
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
			@Override
			public void neutralAction() {
			}
		}
		AlertDialog.Builder dialog = GeneralHelper.buildDialog(message, getString(R.string.continue_string), 
				getString(R.string.cancel_string), this, new RefreshAction(), true, "");
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.browse_new, menu);
		return true;
	}
}
