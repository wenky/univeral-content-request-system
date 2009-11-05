package org.mueller.booklibrary;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Techbook 
{
	String keypath;
	String link;
	String title;
	Set categories; // set of strings...we'll see how hibernate does that...
	
	public Set categoryset() { return categories; }
	public String getCategories() {
		// convert set to categorylist
		Iterator i = categories.iterator();
		String catlist = "";
		boolean first = true;
		while (i.hasNext()) if (first) {first=false; catlist += i.next();} else catlist = catlist+'|'+i.next();
		return catlist;
	}
	public void setCategories(String categories) {
		// break up incoming string
		String[] catlist = categories.split("\\|");		
		Set newcategories = new TreeSet();
		for (int i=0; i < catlist.length; i++) newcategories.add(catlist[i]);		
		this.categories = newcategories;
	}
	public String getKeypath() {
		return keypath;
	}
	public void setKeypath(String keypath) {
		this.keypath = keypath;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
