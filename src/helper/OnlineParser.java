package helper;

import java.io.IOException;
import java.util.ArrayList;

import model.FunctorsInterface;
import model.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.manganotification.R;

import sql.MangaListAccesser;
import sql.MangaListTable.MangaList;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class OnlineParser extends AsyncTask<String, Void, String> {
	public ArrayList<Manga> mangaListParsed;
	public MangaListAccesser mDbHelper;
	public Context context;
	
	public OnlineParser(MangaListAccesser mDbHelper, Context context) {
		this.mDbHelper = mDbHelper;
		this.context = context;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		mangaListParsed = new ArrayList<Manga>();
		try {
			Document doc = Jsoup.connect("http://www.mangareader.net/alphabetical").get();
			Elements mangaListOnline = doc.getElementsByClass("series_col");
			for(Element list : mangaListOnline) {
				Elements linkList = list.getElementsByTag("li");
				for(Element listTag : linkList) {
					Elements linksTag = listTag.getElementsByTag("a");
					Element link = linksTag.get(0);
					String mangaUrl = "http://www.mangareader.net" + link.attr("href");
					if(mangaUrl.equals("#top"))
						continue;
					String mangaTitle = GeneralHelper.capitalize(link.text());
					//Document detailDoc = Jsoup.connect(mangaUrl).get();
					System.out.println("PARSING :" + mangaTitle);
					Manga savedManga = GeneralHelper.getSavedMangaData(mangaTitle, mangaUrl, mDbHelper);
					Manga manga;
					if(savedManga != null) {
						if(savedManga.isSaved == 1) {
							OnlineParseOne oneParser = new OnlineParseOne(mDbHelper, savedManga);
							oneParser.parseMangaDetail();
							manga = oneParser.manga;
						}
						else
							manga = savedManga;
					}
					else
						manga = new Manga(mangaTitle," "," ", " "," ", mangaUrl, " ",0);
					mangaListParsed.add(manga);
				}
			}
		} catch (IOException e) {
			String errMsg = "We're having some trouble connecting to the server\n";
			errMsg += "Sorry for the inconvenience, please try again later!";
			AlertDialog.Builder dialog = GeneralHelper.buildErrorDialog(errMsg,context);
			dialog.show();
		}
		return null;
	}
}

