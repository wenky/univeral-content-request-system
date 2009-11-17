package com.uhg.umvs.bene.cms.contentretrieval.messages;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class URLMessageLookupService implements MessageLookupService
{
    List<String> bundlePaths = null;
    public void setBundlePaths(List<String> filePaths) {this.bundlePaths = filePaths;}
    
    String baseurl = null;
    public void setBaseURL(String url) {baseurl = url;}
    
    
    ResourceBundle getBundle(String path,Locale locale)
    {
        // full passthru logic for now...simple locale + properties construction
        String fullurl = baseurl + path + "_" + locale.getLanguage()+".properties";
        try {
            URL urlobj = new URL(fullurl);
            
            // locate resource properties file or url 
            // get input stream or reader
            InputStream inputstream = null;
            try {
                URLConnection urlconn = urlobj.openConnection();
                inputstream = urlconn.getInputStream();
                ResourceBundle bundle = new PropertyResourceBundle(inputstream);
                return bundle;
            } catch (IOException ioe) {
                return null;
            }
        } catch (MalformedURLException mue) {
            return null;
        }
    }
    
    public MessageFormat resolveCode(String code, Locale locale)
    {
        
        if (bundlePaths == null) return null;
        for (String path : bundlePaths) {
            // load resources one by one
            ResourceBundle bundle = getBundle(path,locale);
            if (bundle != null && bundle.containsKey(code)) {
                String message = bundle.getString(code); 
                MessageFormat msgfmt = new MessageFormat(message);
                return msgfmt;
            }
        }
        
        // not found, ?return default?
        return null;
    }


}
