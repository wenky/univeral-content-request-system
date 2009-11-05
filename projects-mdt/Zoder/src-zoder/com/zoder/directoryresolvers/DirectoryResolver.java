package com.zoder.directoryresolvers;
import com.zoder.util.Context;

public class DirectoryResolver 
{
    public static DirectoryResolver getDirectoryResolver(Context c) throws Exception 
    {
        // check if there's a custom overrider
        if (c.containsKey("Configuration.DirectoryResolver")) {
            String classname = (String)c.get("Configuration.DirectoryResolver");
            return (DirectoryResolver)Class.forName(classname).newInstance();
        }
        // else default
        return new DirectoryResolver();
    }
    
    public String relativeDirectory(String chronicleid, Context c) 
    {
        Integer directorydepth = Integer.parseInt((String)c.get("Configuration.DirectoryDepth"));
        StringBuffer relpath = new StringBuffer(3*directorydepth);
        for (int i=0; i < directorydepth; i++) {
            char alpha = chronicleid.charAt(chronicleid.length()-i*2-1);
            char beta = chronicleid.charAt(chronicleid.length()-i*2-2);
            relpath.append(alpha).append(beta).append('/');
        }
        relpath.append(chronicleid).append('/');
        String path = relpath.toString();
        return path;
    }
    
}
