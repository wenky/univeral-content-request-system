package com.medtronic.documentum.mrcs.docbase.install;

import com.thoughtworks.xstream.*;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

public class ConfigFileReader {
	static Logger logger = Logger.getLogger(ConfigFileReader.class);
	
	private static XStream xs;
	private DocbaseItems dbi;
	
	static{
		xs = new XStream();
        xs.alias("DocbaseItems",DocbaseItems.class);
        xs.alias("Group",Group.class);
        xs.alias("Folder",Folder.class);
        xs.alias("ACL",ACL.class);
        xs.alias("AccessRights",AccessRights.class);
        xs.alias("DocbaseObject",DocbaseObject.class);
        xs.alias("PropertyValue",PropertyValue.class);
	}

	public static void main(String[] args) {
        
	}
	
	public void setConfigFile(String file){
		dbi = loadFile(file);
	}
	
	public DocbaseItems getDocbaseObjects(){
		return dbi;
	}
	
	public static DocbaseItems loadFile(String path) {

		File f = new File(path);
		logger.debug("Loading Config File: " + f.getAbsolutePath());

        BufferedReader filereader = null;
        try {
        	FileInputStream fis = new FileInputStream(f);
        	InputStreamReader isr = new InputStreamReader(fis,"UTF8");
            filereader = new BufferedReader(isr);
        } catch (IOException ioe) {
        	logger.warn("Error loading config file.",ioe);
        }
        
        DocbaseItems items = (DocbaseItems)xs.fromXML(filereader);
		
        try {
        	filereader.close();
        } catch (IOException ioe) {
            logger.warn("Error closing config file.",ioe);
        }
		
        return items;
	}

}

class DocbaseObject{
	DocbaseObject(){}
	public String ObjectName;
	public String ObjectType;
	public String PermissionSet;
	public String FolderPath;
	public HashMap Properties;
}

class PropertyValue{
	PropertyValue(){}
	public String Repeating;
	public List Values;
}

class Group {
	Group(){}
	public String GroupName;
	public String Description;
	public String GroupAddress;
	public List Members;
}

class Folder {
	Folder(){}
	public String FolderName;
	public String FolderType;
	public String ACL;
	public String FolderPath;
}

class AccessRights {
	AccessRights(){}
	public String Permit;
	public List ExtendedPermits;
}

class ACL {
    public static final int PERMIT_NONE = 1;
    public static final int PERMIT_BROWSE = 2;
    public static final int PERMIT_READ = 3;
    public static final int PERMIT_RELATE = 4;
    public static final int PERMIT_VERSION = 5;
    public static final int PERMIT_WRITE = 6;
    public static final int PERMIT_DELETE = 7;
	ACL(){}
	public String ACLName;
	public String Description;
	public HashMap Accessors;
}

class DocbaseItems {
	DocbaseItems(){}
	public List Groups;
	public List Folders;
	public List ACLs;
	public List DocbaseObjects;
}