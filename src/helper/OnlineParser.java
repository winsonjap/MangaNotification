package helper;

import java.util.ArrayList;
import java.util.HashMap;

import model.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sql.MangaListAccesser;

import android.os.AsyncTask;

public class OnlineParser extends AsyncTask<String, Void, String> {
	public ArrayList<Manga> mangaListParsed;
	public ArrayList<Manga> mangaList;
	public MangaListAccesser mDbHelper;
	
	public OnlineParser(MangaListAccesser mDbHelper, ArrayList<Manga> mangaList) {
		this.mDbHelper = mDbHelper;
		this.mangaList = mangaList;
	}
	
	@Override
	protected String doInBackground(String... arg0){
		mangaListParsed = new ArrayList<Manga>();
		try {
			Document doc = Jsoup.connect("http://www.mangareader.net/alphabetical").get();
			Elements mangaListOnline = doc.getElementsByClass("series_col");
			ArrayList<Manga> mangaList = GeneralHelper.getFromDatabase(mDbHelper, false);
			HashMap<String, Manga> mangaHash = GeneralHelper.convertToHash(mangaList);
			for(Element list : mangaListOnline) {
				Elements linkList = list.getElementsByTag("li");
				for(Element listTag : linkList) {
					Elements linksTag = listTag.getElementsByTag("a");
					Element link = linksTag.get(0);
					String mangaUrl = "http://www.mangareader.net" + link.attr("href");
					if(mangaUrl.equals("#top"))
						continue;
					String mangaTitle = GeneralHelper.capitalize(link.text());
					System.out.println("PARSING :" + mangaTitle);
					Manga savedManga = null;
					if(mangaHash.containsKey(mangaUrl))
						savedManga = mangaHash.get(mangaUrl);
					//Manga savedManga = GeneralHelper.getSavedMangaData(db, mangaTitle, mangaUrl, mDbHelper);
					Manga manga;
					if(savedManga != null) {
//						if(savedManga.isSaved == 1) {
//							OnlineParseOne oneParser = new OnlineParseOne(mDbHelper, savedManga);
//							oneParser.parseMangaDetail();
//							manga = oneParser.manga;
//						}
//						else
							manga = savedManga;
						manga.id = -1;
					}
					else
						manga = new Manga(-1, mangaTitle," "," ", " "," ", mangaUrl, " ",0);
					mangaListParsed.add(manga);
				}
			}
		} catch (Exception e) {
			mangaListParsed = new ArrayList<Manga>();
			String errMsg = "We're having some trouble connecting to the server\n";
			errMsg += "Sorry for the inconvenience, please try again later!";
			System.out.println(errMsg);
		}
		return null;
	}
}

