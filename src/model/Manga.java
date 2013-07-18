package model;

import java.util.ArrayList;
import java.util.Date;


public class Manga implements Comparable{
	public String title = "";
	public String author;
	public String newestChapter;
	public String readChapter;
	public String lastUpdated;
	public String url;
	public String imgUrl;
	public int isSaved;
	
	public Manga(String title, String author, String newestChapter, 
				 String readChapter, String lastUpdated, 
				 String url, String imgUrl, int isSaved) {
		this.title = title;
		this.author = author;
		this.newestChapter = newestChapter;
		this.lastUpdated = lastUpdated;
		this.readChapter = readChapter;
		this.url = url;
		this.imgUrl = imgUrl;
		this.isSaved = isSaved;
	}

	@Override
	public int compareTo(Object arg0) {
		String objTitle = ((Manga)(arg0)).title;
		return this.title.compareTo(objTitle);
	}
	
	@Override
	public String toString() {
		return "Manga title: "+this.title + ", author: " + this.author;
	}
}

