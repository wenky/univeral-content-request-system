package com.medtronic.ecm.documentum.core.web.install;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

public class DocbaseObjectsConfigFileReader {
    
    private static XStream xs;
    private DocbaseItems dbi;
    
    static{
        xs = new XStream();
        xs.alias("DocbaseItems",DocbaseItems.class);
        xs.alias("User",User.class);
        xs.alias("Group",Group.class);
        xs.alias("Folder",Folder.class);
        xs.alias("ACL",ACL.class);
        xs.alias("AccessRights",AccessRights.class);
        xs.alias("DocbaseObject",DocbaseObject.class);
        xs.alias("PropertyValue",PropertyValue.class);
    }
    
    
    public static DocbaseItems deserialize(String xmldoc, Map status) throws Exception {
        
        BufferedReader reader = new BufferedReader(new StringReader(xmldoc));        
        try { 
            /*-msg-*/Util.msg("reading xmldoc", status);
            DocbaseItems items = (DocbaseItems)xs.fromXML(reader);
            reader.close();
            /*-msg-*/Util.msg("successfully read xmldoc",status);
            return items;
        } catch (Exception e) {
            Util.msg("error in reading xmldoc: "+e.getMessage(), status);
            throw e;
        }
    }

}

class User {
    public String UserName;
    public String LoginName;
    public String PSrc;
    public String PCred;
    public String Email;    
    public String Home;
    public String P;
    public boolean ACFG,APRG,AVW;
    public String C;
    public int FailCount = -1;
    public List Groups;
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
    public String ClearUsers;
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
    public List Users;
    public List Groups;
    public List Folders;
    public List ACLs;
    public List DocbaseObjects;
}