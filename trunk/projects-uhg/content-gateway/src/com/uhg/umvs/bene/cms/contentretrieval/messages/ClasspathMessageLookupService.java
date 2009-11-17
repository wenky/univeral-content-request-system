package com.uhg.umvs.bene.cms.contentretrieval.messages;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ClasspathMessageLookupService implements MessageLookupService
{
    List<String> bundlePaths = null;
    public void setBundlePaths(List<String> filePaths) {this.bundlePaths = filePaths;}

    
    ResourceBundle getBundle(String path,Locale locale)
    {        
        ResourceBundle bundle = ResourceBundle.getBundle(path,locale);
        return bundle;
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
