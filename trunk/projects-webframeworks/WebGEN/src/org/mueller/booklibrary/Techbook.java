package org.mueller.booklibrary;

public class Techbook 
{
	String keypath;
	String link;
	String title;
	String categories;
	int format; 
	
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
	public String getCategories() {
		return categories; 
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	
}
