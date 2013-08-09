package model;

import helper.GeneralHelper;
import helper.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.example.manganotification.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
/**
 * Layout of the list view
 * It will show the layout
 * @author winson
 *
 * @param <T>
 */
public class AZAdapter<T> extends ArrayAdapter<Manga> implements SectionIndexer, Filterable {
	private ArrayList<String> mangaTitles;
	private HashMap<String, Integer> azIndexer;
	private String[] sections;
	public ArrayList<Manga> mangaList;
	public ArrayList<Manga> origMangaList;
	private ImageLoader imageLoader;

	public AZAdapter(Context context, int textViewResourceId, ArrayList<Manga> objects) {
		super(context, textViewResourceId, objects);
		this.origMangaList = objects;
		this.mangaList = objects;
		this.imageLoader=new ImageLoader(context.getApplicationContext());
		this.setIndexer();
	}
	
	/**
	 * Set alphabet indexer on the right side of the page
	 */
	public void setIndexer() {
		mangaTitles = getMangaTitle(mangaList);
		azIndexer = new HashMap<String, Integer>(); //stores the positions for the start of each letter
		int size = mangaTitles.size();
		for (int i = size - 1; i >= 0; i--) {
			String element = mangaTitles.get(i);
			//We store the first letter of the word, and its index.
			if(!GeneralHelper.isEmptyStringChecker(element))
				azIndexer.put(element.substring(0, 1), i); 
		} 
		Collections.sort(mangaTitles);
		Set<String> keys = azIndexer.keySet(); // set of letters 

		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>(); 

		while (it.hasNext()) {
			String key = it.next();
			keyList.add(key);
		}
		Collections.sort(keyList);//sort the keylist
		sections = new String[keyList.size()]; // simple conversion to array            
		keyList.toArray(sections);
	}
	
	/**
	 * Function to get list of all titles as array
	 * @param mangaList - array of manga to get titles from
	 * @return the list of titles (string)
	 */
	private ArrayList<String> getMangaTitle(ArrayList<Manga> mangaList) {
		ArrayList<String> ret = new ArrayList<String>();
		String currTitle;
		for(Manga manga : mangaList) {
        	currTitle = manga.title;
        	ret.add(capitalize(currTitle));
        }
        return ret;
	}
	
	/**
	 * Function to capitalize string
	 * @param string - to be capitalized
	 * @return capitalized string
	 */
	private String capitalize(String string)
	{
		if (string == null)
	        throw new NullPointerException("no string ");
		if(string == "" || string == " ")
			return string;
	    return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
	
	/**
	 * Function to show the view of each row
	 */
	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;
		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.manga_list, null);
		}
		
		Manga manga;
		if(position >= mangaList.size())
			manga = new Manga(-1,"","","","","","","",0);
		else
			manga= mangaList.get(position);
		TextView title = (TextView) v.findViewById(R.id.title);
		TextView author = (TextView) v.findViewById(R.id.author);
		TextView last_chapter = (TextView) v.findViewById(R.id.last_chapter);
		TextView read_chapter = (TextView) v.findViewById(R.id.read_chapter);
		ImageView img = (ImageView) v.findViewById(R.id.list_image);
		ImageView check_img = (ImageView) v.findViewById(R.id.check_image);

		title.setText(manga.title);
		author.setText(manga.author);
		if(manga.isSaved==1 && !manga.readChapter.equals(manga.newestChapter)) {
			if(GeneralHelper.isEmptyStringChecker(manga.readChapter))
				manga.readChapter = "0";
			read_chapter.setText("Last Read : " + manga.readChapter);
		}
		else
			read_chapter.setText("");
		
		if(!GeneralHelper.isEmptyStringChecker(manga.newestChapter))
			last_chapter.setText("Last Chapter : " + manga.newestChapter);
		else
			last_chapter.setText("");
		if(manga.isSaved == 1)
			check_img.setImageResource(R.drawable.ic_menu_check);
		else
			check_img.setImageResource(R.drawable.ic_menu_xmark);
		imageLoader.DisplayImage(manga.imgUrl, img);
		// the view must be returned to our activity
		return v;

	}

	@Override
	public int getPositionForSection(int section) {
		if(sections.length > 1) {
			String letter = sections[section];
			return azIndexer.get(letter);
		}
		else if(sections.length == 1) { //if there's only 1 item it will throw error
			String letter = sections[0];
			return azIndexer.get(letter);
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		Log.v("getSectionForPosition", "called");
		return 0;
	}

	@Override
	public Object[] getSections() {
		return sections; // to string will be called to display the letter
	}
	
	/**
	 * Filtering
	 */
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mangaList = (ArrayList<Manga>) results.values;
				setIndexer();
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				ArrayList<Manga> FilteredArrayNames = new ArrayList<Manga>();
				// perform your search here using the searchConstraint String.
				constraint = constraint.toString().toLowerCase();
				for (int i = 0; i < origMangaList.size(); i++) {
					Manga currManga = origMangaList.get(i);
					String dataNames = currManga.title;
					if (dataNames.toLowerCase().indexOf(constraint.toString()) != -1)  {
						FilteredArrayNames.add(currManga);
					}
				}
				results.count = FilteredArrayNames.size();
				results.values = FilteredArrayNames;
				//Log.e("VALUES", results.values.toString());
				return results;
			}
		};

		return filter;
	}
}

