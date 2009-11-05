package com.zoder.util;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

// convenience methods for playing with DCTM folder paths


public class FolderPathUtils 
{

    // swiped from wdk. is this reliable????
    public static boolean isFolderType(String strObjectId)
    {
        boolean bIsFolderType = false;
        if(strObjectId != null)
        {
            if(strObjectId.length() == 16 && strObjectId.charAt(0) == '0' && (strObjectId.charAt(1) == 'b' || strObjectId.charAt(1) == 'c'))
                bIsFolderType = true;
        } else
        {
            throw new IllegalArgumentException("Object Id cannot be null!");
        }
        return bIsFolderType;
    }

    
    
    public static String getFirstPathItem(String path)
    {
        if (path == null) return null;
        int len = path.length(); 
        if (len == 0) return "";        
        StringBuffer buf = new StringBuffer(); //def to 16
        int i=0; 
        if (path.charAt(0) == '/') {
            i++;
            if (len == 1) return "";
        }
        while (true) {
            if (i == len) 
                return buf.toString();
            if (path.charAt(i) == '/') 
                return buf.toString();            
            buf.append(path.charAt(i));
            i++;
        }
    }
    
    public static String[] pathToNameArray(String path)
    {
        path = normalizePath(path);
        String[] patharray = path.substring(1).split("/");
        return patharray;
    }
    
    // make sure there's a front /, no trailing /
    public static String normalizePath(String path) 
    {
        if (path.charAt(0) == '/') {
            if (path.charAt(path.length()-1) !='/') { 
                return path;
            } else {
                return path.substring(0,path.length()-1);
            }
        } else {
            if (path.charAt(path.length()-1) !='/') { 
                return "/"+path;
            } else {
                return "/"+path.substring(0,path.length()-1);
            }
            
        }
    }
    
    public static IDfFolder getFolderObject(IDfSession session, String path) throws DfException{
        // handles cabinets...screwy dfc, tsk tsk
        IDfFolder fldobj = (IDfFolder)session.getFolderByPath(path);
        if (fldobj == null) {
            // probably a cabinet
            fldobj = (IDfFolder)session.getObjectByQualification("dm__cabinet where object_name = '"+FolderPathUtils.getFirstPathItem(path)+"'");
            if (fldobj == null) {
                // bloody hell...
                throw new NullPointerException("Invalid folder path: "+path);
            }
        }
        return fldobj;
    }
    
    public static IDfFolder[] pathToFolderArray(IDfSession session, String path) throws DfException {
        String[] patharray = pathToNameArray(path);
        IDfFolder[] objarray = new IDfFolder[patharray.length];
        String curpath = "/";
        
        for (int i=0;i < patharray.length; i++) {
            curpath += patharray[i];
            IDfFolder f = getFolderObject(session,curpath);
            objarray[i] = f;
        }
        return objarray;
    }
    
    public static String pathToIdChain(IDfSession session, String path) throws DfException {
        String[] patharray = pathToNameArray(path);
        String curpath = "/";
        StringBuffer idchain = new StringBuffer(100);
        
        for (int i=0;i < patharray.length; i++) {
            curpath += patharray[i];
            IDfFolder f = getFolderObject(session,curpath);
            if (i == 0)
                idchain.append(f.getObjectId().getId());
            else
                idchain.append('-').append(f.getObjectId().getId());
        }
        return idchain.toString();
    }

    public static String[] pathToIdArray(IDfSession session, String path) throws DfException {
        String[] patharray = pathToNameArray(path);
        String[] idarray = new String[patharray.length];
        String curpath = "/";
        StringBuffer idchain = new StringBuffer(100);
        
        for (int i=0;i < patharray.length; i++) {
            curpath += patharray[i];
            IDfFolder f = getFolderObject(session,curpath);
            idarray[i] = f.getObjectId().getId();
        }
        return idarray;
    }


    public static int getPathDepth(String strFolderPath)
    {
        int nFolders = 0;
        strFolderPath = normalizePath(strFolderPath);
        for(int i = 0; i < strFolderPath.length(); i++)
            if(strFolderPath.charAt(i) == '/')
                nFolders++;
        return nFolders;
    }



}

    
  