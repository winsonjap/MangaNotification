package helper;

import java.io.IOException;
import java.util.regex.Pattern;

import model.Manga;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import sql.MangaListAccesser;

import android.os.AsyncTask;

/**
 * Class to parse one manga to get the detail from online information
 * @author winson
 *
 */
public class OnlineParseOne extends AsyncTask<String, Void, String> {
	public MangaListAccesser mDbHelper;
	public Manga manga;

	public OnlineParseOne(MangaListAccesser mDbHelper, Manga manga) {
		this.mDbHelper = mDbHelper;
		this.manga = manga;
	}

	@Override
	protected String doInBackground(String... arg0) {
		parseMangaDetail();
		return null;
	}

	public void parseMangaDetail() {
		try {
			Document detailDoc = Jsoup.connect(manga.url).get();
			manga.author = this.getAuthor(detailDoc);
			manga.newestChapter = this.getLastChapter(detailDoc,manga);
			manga.imgUrl = this.getImgUrlTag(detailDoc);
			manga.lastUpdated = this.getLastUpdatedDate(detailDoc);
		} catch (IOException e) {
			System.err.println("We're having some trouble connecting to the server");
			System.err.println("Sorry for the inconvenience, please try again later!");
		}
	}

	private String getImgUrlTag(Document detailDoc) {
		Element imgDiv = detailDoc.getElementById("mangaimg");
		Elements imgUrls = imgDiv.getElementsByTag("img");
		Element imgUrlTag = imgUrls.get(0);
		return imgUrlTag.attr("src");
	}

	private String getLastChapter(Document detailDoc, Manga manga) {
		Element latestChaptersId = detailDoc.getElementById("latestchapters");
		Elements latestChapters = latestChaptersId.getElementsByTag("li");
		if(latestChapters.size() == 0)
			return "-1";
		Element firstElement = latestChapters.first();
		Elements links = firstElement.getElementsByTag("a");
		Element link = links.first();
		String text = link.text();
		text = text.replaceAll(Pattern.quote(manga.title+" "), "");
		return text;
		//		System.out.println("CURR : " + manga.title);
		//		Element detailElements = detailDoc.getElementById("listing");
		//		Elements trList = detailElements.getElementsByAttribute("tr");
		//		Element lastChap = trList.last();
		//		Elements tdList = lastChap.getElementsByAttribute("td");
		//		Element lastChapNum = tdList.first();
		//		String text = lastChapNum.text();
		//		text = text.replaceAll(manga.title +" ", "");
		//		return text;
	}

	private String getLastUpdatedDate(Document detailDoc) {
		Element detailElements = detailDoc.getElementById("chapterlist");
		Elements trList = detailElements.getElementsByTag("td");
		Element lastChap = trList.last();
		//Elements tdList = lastChap.getElementsByAttribute("td");
		//if(tdList.size() < 2)
		//	return "-1";
		//Element dateTd = tdList.last();
		return lastChap.text();
	}

	private String getAuthor(Document detailDoc) {
		String authors = "";
		Element mangaProp = detailDoc.getElementById("mangaproperties");
		Elements propertyTitle = mangaProp.getElementsByTag("tr");
		for(Element eachProp : propertyTitle) {
			Elements tdList = eachProp.getElementsByTag("td");
			String prop = tdList.first().text();
			String val = tdList.last().text();
			System.out.println("PROP : " + prop);
			System.out.println("VAL : " + val);
			if(prop.equals("Author:")) {
				authors += val;
			}
			else if(prop.equals("Artists:")) {
				if(val!="") {
					if(authors!="")
						authors += " - ";
					authors += val;
				}
			}
		}
		return authors;
	}
}
