package com.medtronic.ecm.documentum.core.web.install;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

// service class for installing objects into documentum 
// adaptation of com.medtronic.documentum.mrcs.docbase.install.* from MRCS 4.2 architecture
// inputs: username, password, docbase, and xml content
// outputs: validations, errors, or success


public class DocbaseObjectInstallationService 
{
    public static Map processObjectScript(String user, String pass, String base, String xmldoc)
    {
        Map status = new HashMap();
        List messages = new ArrayList();
        status.put("messages", messages);
        IDfSessionManager smgr = null;
        try {
            /*-msg-*/messages.add("Creating session manager for user "+user+" docbase "+base);
            smgr = getSessionMgr(user,pass,base,status);
        } catch (Exception e) {
            // create status message, return
            /*-msg-*/messages.add("Error creating session manager "+ e.getMessage());
            return status;
        }
        
        try {
            
            DocbaseItems dbi = DocbaseObjectsConfigFileReader.deserialize(xmldoc, status);
            processGroups(dbi,status,smgr,base);
            processUsers(dbi,status,smgr,base);
            processACLs(dbi,status,smgr,base);
            processFolders(dbi,status,smgr,base);
            processObjects(dbi,status,smgr,base);
            
        } catch (DfException dfe) {
            /*-msg-*/messages.add("Terminating DfException: "+dfe.getStackTraceAsString());            
            return status;
        } catch (Exception e) {
            /*-msg-*/messages.add("Terminating Exception: "+e.getMessage());            
            return status;
        } 
        
        return status;        
    }
    
    public static IDfSessionManager getSessionMgr(String user, String pass, String base, Map status) throws DfException
    {
        IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();
        IDfSessionManager sMgr = client.newSessionManager();
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(user);
        loginInfoObj.setPassword(pass);
        loginInfoObj.setDomain(null);
        sMgr.setIdentity(base, loginInfoObj);
        return sMgr;
    }

    public static void processGroups(DocbaseItems dbi, Map status, IDfSessionManager sMgr, String docbase) throws Exception
    {
        if (dbi.Groups == null) return;
        IDfSession session = null;
        try {
            session = sMgr.getSession(docbase);

            List groups = dbi.Groups;

            ListIterator groupIter = groups.listIterator();

            while (groupIter.hasNext()){
                Group grp = (Group)groupIter.next();
                /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": creation started");

                sMgr.beginTransaction();

                //see if the group exists
                IDfGroup group = session.getGroup(grp.GroupName);

                if (group == null) {
                    /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": does not exist");
                    group = (IDfGroup)session.newObject("dm_group");
                    group.setGroupName(grp.GroupName);
                }
                else {
                    /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": exists");
                    if (grp.ClearUsers != null) {                    
                        /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": clearing out subgroups and users");
                        group.removeAllGroups();
                        group.removeAllUsers();
                    }
                }

                group.setDescription(grp.Description);
                group.setGroupAddress(grp.GroupAddress);

                List members = grp.Members;

                ListIterator memIter = members.listIterator();

                while (memIter.hasNext()){
                    /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": has members");
                    String member = (String)memIter.next();
                    boolean isGroup = !(session.getGroup(member) == null);
                    boolean isUser = !(session.getUser(member) == null);
                    if (isUser) {
                        if (group.findString("users_names", member) == -1) {
                            /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": ADDING user "+member);
                            group.addUser(member);
                        }
                    }
                    else if (isGroup){
                        if (group.findString("groups_names", member) == -1) {
                            /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": ADDING subgroup "+member);
                            group.addGroup(member);
                        }
                    }
                    else {
                        /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": member not valid: " + member);
                    }
                }

                //save the group
                group.save();
                /*-msg-*/Util.msg(status,"Group: " + grp.GroupName + ": created");

                //end the transaction
                sMgr.commitTransaction();
            }

        } catch (DfException dfe){
            /*-msg-*/Util.msg(status,dfe.getStackTraceAsString());
            sMgr.setTransactionRollbackOnly();
            throw dfe;
        } finally {
            sMgr.release(session);
        }

    }
    
    public static void processUsers(DocbaseItems dbi, Map status, IDfSessionManager sMgr, String docbase) throws Exception
    {
        if (dbi.Users == null) return;
        IDfSession session = null;
        try {
            session = sMgr.getSession(docbase);

            List users = dbi.Users;

            ListIterator userIter = users.listIterator();

            while (userIter.hasNext()){
                User usr = (User)userIter.next();
                /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": creation started");

                sMgr.beginTransaction();

                //see if the group exists
                IDfUser user = session.getUser(usr.UserName);

                if (user == null) {
                    /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": does not exist, CREATING");
                    user = (IDfUser)session.newObject("dm_user");
                    user.setUserName(usr.UserName);
                }                
                user.setUserLoginName(usr.LoginName);
                if (usr.PSrc == null || "".equals(usr.PSrc) || "inline".equals(usr.PSrc)) {
                    user.setUserSourceAsString("inline password");
                    user.setUserPassword(usr.PCred);
                } else {
                    user.setUserSourceAsString(usr.PSrc);
                    // LDAP?
                }
                user.setUserAddress(usr.Email);
                
                // user privledges
                if (usr.P == null) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_NONE);
                else if ("cabinet".equalsIgnoreCase(usr.P)) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_CREATE_CABINET);
                else if ("group".equalsIgnoreCase(usr.P)) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_CREATE_GROUP);
                else if ("type".equalsIgnoreCase(usr.P)) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_CREATE_TYPE);
                else if ("sysadmin".equalsIgnoreCase(usr.P)) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_SYSADMIN);
                else if ("superuser".equalsIgnoreCase(usr.P)) user.setUserPrivileges(IDfUser.DF_PRIVILEGE_SUPERUSER);
                else {
                    /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": unknown priv "+usr.P);
                    user.setUserPrivileges(IDfUser.DF_PRIVILEGE_NONE);
                }

                // user xprivs
                int xpriv = 0;
                if (usr.ACFG) xpriv |= IDfUser.DF_XPRIVILEGE_CONFIG_AUDIT;
                if (usr.APRG) xpriv |= IDfUser.DF_XPRIVILEGE_PURGE_AUDIT;
                if (usr.AVW) xpriv |= IDfUser.DF_XPRIVILEGE_VIEW_AUDIT;
                user.setUserXPrivileges(xpriv);
                
                // client capability
                if (usr.C == null) user.setClientCapability(IDfUser.DF_CAPABILITY_NONE);
                else if ("consumer".equalsIgnoreCase(usr.C)) user.setClientCapability(IDfUser.DF_CAPABILITY_CONSUMER);
                else if ("contributor".equalsIgnoreCase(usr.C)) user.setClientCapability(IDfUser.DF_CAPABILITY_CONTRIBUTOR);
                else if ("coordinator".equalsIgnoreCase(usr.C)) user.setClientCapability(IDfUser.DF_CAPABILITY_COORDINATOR);
                else if ("sysadmin".equalsIgnoreCase(usr.C)) user.setClientCapability(IDfUser.DF_CAPABILITY_SYSTEM_ADMIN);
                else {
                    /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": unknown priv "+usr.P);
                    user.setClientCapability(IDfUser.DF_CAPABILITY_CONSUMER);
                }                
                user.setFailedAuthenticationAttempts(usr.FailCount);
               
                if (usr.Home != null) user.setDefaultFolder(usr.Home, true);


                //save the group
                user.save();
                /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": created");
                
                if (usr.Groups != null) {

                    ListIterator grpIter = usr.Groups.listIterator();
    
                    while (grpIter.hasNext()){
                        String groupname = (String)grpIter.next();
                        /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": being added to group "+ groupname);
                        IDfGroup groupobj = (IDfGroup)session.getGroup(groupname);
                        boolean isGroup = (groupobj != null);
                        if (isGroup){
                            groupobj.addUser(usr.UserName);
                            groupobj.save();
                        }
                        else {
                            /*-msg-*/Util.msg(status,"User: " + usr.UserName + ": not a valid group: " + groupname);
                        }
                    }
                }                    

                //end the transaction
                sMgr.commitTransaction();
            }

        } catch (DfException dfe){
            /*-msg-*/Util.msg(status,dfe.getStackTraceAsString());
            sMgr.setTransactionRollbackOnly();
            throw dfe;
        } finally {
            sMgr.release(session);
        }

    }
    
    public static void processACLs(DocbaseItems dbi, Map status, IDfSessionManager sMgr, String docbase) throws Exception
    {
        if (dbi.ACLs == null) return;
        IDfSession session = null;
        try {
            session = sMgr.getSession(docbase);

            List ACLs = dbi.ACLs;

            ListIterator aclIter = ACLs.listIterator();

            while (aclIter.hasNext()){
                ACL acl = (ACL)aclIter.next();
                /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": creation started");

                sMgr.beginTransaction();

                //get the acl domain
                IDfTypedObject serverConfig = session.getServerConfig();
                String aclDomain = serverConfig.getString("operator_name");

                //see if the acl exists
                IDfACL pSet = session.getACL(aclDomain, acl.ACLName);

                if (pSet == null) {
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": does not exist");
                    pSet = (IDfACL)session.newObject("dm_acl");
                    pSet.setObjectName(acl.ACLName);
                    pSet.setDomain(aclDomain);
                }
                else {
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": exists");
                }

                //set the description
                pSet.setDescription(acl.Description);

                pSet.setACLClass(0);
                pSet.save();
                /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": saved the first time");

                //set the accessors
                Set accessorSet = acl.Accessors.keySet();

                Iterator accessorKeys = accessorSet.iterator();

                while (accessorKeys.hasNext()){
                    String accessor = (String)accessorKeys.next();
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": accessor: " + accessor);
                    AccessRights ar = (AccessRights)acl.Accessors.get(accessor);
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": permit: " + ar.Permit);

                    String xPermits = null;
                    Iterator xPermitIter = ar.ExtendedPermits.iterator();

                    if (!ar.ExtendedPermits.isEmpty()) xPermits = (String)xPermitIter.next();
                    while (xPermitIter.hasNext()){
                        xPermits += "," + (String)xPermitIter.next();
                    }
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": xpermits: " + xPermits);
                    
                    //pSet.grant(accessor,processAccess(ar.Permit),xPermits);
                    //pSet.revoke(accessor,"EXECUTE_PROC,CHANGE_LOCATION,CHANGE_STATE,CHANGE_PERMIT,CHANGE_OWNER");
                    pSet.grant(accessor,processAccess(ar.Permit),xPermits);
                    //if (!ar.ExtendedPermits.contains("EXECUTE_PROC")) pSet.revoke(accessor,"EXECUTE_PROC");
                    //if (!ar.ExtendedPermits.contains("CHANGE_LOCATION")) pSet.revoke(accessor,"CHANGE_LOCATION");
                    
                    /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": accessor: " + accessor + " granted permissions");
                }

                pSet.save();
                /*-msg-*/Util.msg(status,"ACL: " + acl.ACLName + ": saved the last time");

                //end the transaction
                sMgr.commitTransaction();
            }

        } catch (DfException dfe){
            /*-msg-*/Util.msg(status,dfe.getStackTraceAsString());
            sMgr.setTransactionRollbackOnly();
            throw dfe;
        } finally {
            sMgr.release(session);
        }

    }
    
    private static int processAccess(String permit){
        if (permit.equalsIgnoreCase("None"))
            return ACL.PERMIT_NONE;
        if (permit.equalsIgnoreCase("Browse"))
            return ACL.PERMIT_BROWSE;
        if (permit.equalsIgnoreCase("Read"))
            return ACL.PERMIT_READ;
        if (permit.equalsIgnoreCase("Relate"))
            return ACL.PERMIT_RELATE;
        if (permit.equalsIgnoreCase("Version"))
            return ACL.PERMIT_VERSION;
        if (permit.equalsIgnoreCase("Write"))
            return ACL.PERMIT_WRITE;
        if (permit.equalsIgnoreCase("Delete"))
            return ACL.PERMIT_DELETE;
        return 0;
    }
    
    public static void processFolders(DocbaseItems dbi, Map status, IDfSessionManager sMgr, String docbase) throws Exception 
    {
        if (dbi.Folders == null) return;
        IDfSession session = null;
        try {
            session = sMgr.getSession(docbase);

            List folders = dbi.Folders;

            ListIterator folderIter = folders.listIterator();

            while (folderIter.hasNext()){
                Folder fldr = (Folder)folderIter.next();
                /*-msg-*/Util.msg(status,"Folder: " + fldr.FolderName + ": creation started");

                sMgr.beginTransaction();

                //see if the folder exists
                IDfFolder folder = session.getFolderByPath(fldr.FolderPath + "/" + fldr.FolderName);

                //get the acl domain
                IDfTypedObject serverConfig = session.getServerConfig();
                String aclDomain = serverConfig.getString("operator_name");
                /*-msg-*/Util.msg(status,"Folder: " + fldr.FolderName + ": acl domain: " + aclDomain);

                //build an acl reference
                IDfACL acl = session.getACL(aclDomain, fldr.ACL);

                if (folder == null) {
                    /*-msg-*/Util.msg(status,"Folder: " + fldr.FolderName + ": does not exist");
                    folder = (IDfFolder)session.newObject(fldr.FolderType);
                    folder.setObjectName(fldr.FolderName);
                    folder.setACL(acl);
                    if (!fldr.FolderType.equals("dm_cabinet")) folder.link(fldr.FolderPath);

                }
                else {
                    /*-msg-*/Util.msg(status,"Folder: " + fldr.FolderName + ": exists");
                    folder.setObjectName(fldr.FolderName);
                    folder.setACL(acl);
                }

                //save the folder
                folder.save();
                /*-msg-*/Util.msg(status,"Folder: " + fldr.FolderName + ": created");

                //end the transaction
                sMgr.commitTransaction();
            }

        } catch (DfException dfe){
            /*-msg-*/Util.msg(status,dfe.getStackTraceAsString());
            sMgr.setTransactionRollbackOnly();
        } finally {
            sMgr.release(session);
        }

    }
    
    public static void processObjects(DocbaseItems dbi, Map status, IDfSessionManager sMgr, String docbase) throws Exception
    {
        if (dbi.DocbaseObjects == null) return;
        IDfSession session = null;
        try {
            session = sMgr.getSession(docbase);

            List objects = dbi.DocbaseObjects;

            ListIterator objIter = objects.listIterator();

            while (objIter.hasNext()){
                DocbaseObject dbo = (DocbaseObject)objIter.next();

                /*-msg-*/Util.msg(status,dbo.ObjectName + ": creation started");

                sMgr.beginTransaction();

                //get the acl domain
                IDfTypedObject serverConfig = session.getServerConfig();
                String aclDomain = serverConfig.getString("operator_name");

                //see if the object exists
                IDfSysObject dbObject = (IDfSysObject)session.getObjectByQualification(dbo.ObjectType + " where object_name = '" + dbo.ObjectName + "' and Folder('" + dbo.FolderPath + "')");

                //build an acl reference
                IDfACL acl = session.getACL(aclDomain, dbo.PermissionSet);

                //create this object in the docbase
                if (dbObject == null) {
                    //create the object
                    dbObject = (IDfSysObject)session.newObject(dbo.ObjectType);

                    dbObject.link(dbo.FolderPath);
                }
                else {
                    /*-msg-*/Util.msg(status,dbo.ObjectName + ": exists");
                }

                //set the object name
                dbObject.setObjectName(dbo.ObjectName);

                //set the ACL
                dbObject.setACL(acl);

                //set the properties
                Set propertySet = dbo.Properties.keySet();

                Iterator propertyKeys = propertySet.iterator();

                while (propertyKeys.hasNext()){
                    String propertyName = (String)propertyKeys.next();

                    /*-msg-*/Util.msg(status,dbo.ObjectName + ": property: " + propertyName);
                    PropertyValue pv = (PropertyValue)dbo.Properties.get(propertyName);
                    /*-msg-*/Util.msg(status,dbo.ObjectName + ": repeating: " + pv.Repeating);
                    boolean isRepeating = Boolean.valueOf(pv.Repeating).booleanValue();
                    if (isRepeating) {
                        dbObject.removeAll(propertyName); // CEM - need to clear list so old values don't stick around...                       
                    }
                    Iterator propertyValues = pv.Values.iterator();
                    while (propertyValues.hasNext()){
                        String value = (String)propertyValues.next();
                        if (!isRepeating) {
                            dbObject.setString(propertyName,value);
                            /*-msg-*/Util.msg(status,dbo.ObjectName + ": single valued property: " + value);
                        }
                        else {
                            dbObject.appendString(propertyName,value);
                            /*-msg-*/Util.msg(status,dbo.ObjectName + ": repeating property: " + value);
                        }
                    }

                }

                //save the object
                dbObject.save();

                /*-msg-*/Util.msg(status,dbo.ObjectName + ": saved");

                //end the transaction
                sMgr.commitTransaction();

            }

        } catch (DfException dfe){
            /*-msg-*/Util.msg(status,dfe.getStackTraceAsString());
            sMgr.setTransactionRollbackOnly();
        } finally {
            sMgr.release(session);
        }

    }


    


}
