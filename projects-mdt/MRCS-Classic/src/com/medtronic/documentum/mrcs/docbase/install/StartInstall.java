package com.medtronic.documentum.mrcs.docbase.install;

import com.documentum.com.*;
import com.documentum.fc.common.*;
import com.documentum.fc.client.*;
//import com.documentum.fc.common.DfLogger;
import org.apache.log4j.Logger;

import java.util.*;

public class StartInstall {

	static Logger logger = Logger.getLogger(StartInstall.class);

	private static DocbaseItems dbi;
	private IDfSessionManager sMgr;
	private String docbase;
	private String username;
	private String password;

	public static void main(String[] args) {
        if (args.length == 4) {

        	logger.debug("Starting Installation.");

    		StartInstall si = new StartInstall();

    		si.docbase = args[1];
    		logger.debug("Setting docbase: " + si.docbase);
    		si.username = args[2];
    		logger.debug("Setting username: " + si.username);
    		si.password = args[3];
    		logger.debug("Setting password: *******");

    		si.setSessionMgr();

    		dbi = ConfigFileReader.loadFile(args[0]);
    		si.processGroups();
    		si.processACLs();
    		si.processFolders();
    		si.processObjects();

        	logger.debug("Installation Complete.");
        }
        else {
            System.out.println("Usage:  StartInstall [config] [docbase] [username] [password]");
            if (args.length > 0) System.out.println(args[0]);
            if (args.length > 1) System.out.println(args[1]);
            if (args.length > 2) System.out.println(args[2]);
            if (args.length > 3) System.out.println(args[3]);
        }
	}

	public void processGroups() {
		IDfSession session = null;
		try {
			session = sMgr.getSession(docbase);

			List groups = dbi.Groups;

			ListIterator groupIter = groups.listIterator();

			while (groupIter.hasNext()){
				Group grp = (Group)groupIter.next();
				logger.debug("Group: " + grp.GroupName + ": creation started");

				sMgr.beginTransaction();

				//see if the group exists
				IDfGroup group = session.getGroup(grp.GroupName);

				if (group == null) {
					logger.debug("Group: " + grp.GroupName + ": does not exist");
					group = (IDfGroup)session.newObject("dm_group");
					group.setGroupName(grp.GroupName);
				}
	            else {
	            	logger.debug("Group: " + grp.GroupName + ": exists");
	                group.removeAllGroups();
	                group.removeAllUsers();
	            }

	            group.setDescription(grp.Description);
	            group.setGroupAddress(grp.GroupAddress);

	            List members = grp.Members;

	            ListIterator memIter = members.listIterator();

	            while (memIter.hasNext()){
	            	logger.debug("Group: " + grp.GroupName + ": has members");
	            	String member = (String)memIter.next();
	            	boolean isGroup = !(session.getGroup(member) == null);
	            	boolean isUser = !(session.getUser(member) == null);
	            	if (isUser) {
	            		group.addUser(member);
	            	}
	            	else if (isGroup){
	            		group.addGroup(member);
	            	}
	            	else {
	            		logger.debug("Group: " + grp.GroupName + ": member not valid: " + member);
	            	}
	            }

	            //save the group
	            group.save();
	            logger.debug("Group: " + grp.GroupName + ": created");

	            //end the transaction
	            sMgr.commitTransaction();
			}

		} catch (DfException dfe){
			logger.warn(dfe.getStackTraceAsString());
			sMgr.setTransactionRollbackOnly();
		} finally {
			sMgr.release(session);
		}

	}

	public void processFolders() {
		IDfSession session = null;
		try {
			session = sMgr.getSession(docbase);

			List folders = dbi.Folders;

			ListIterator folderIter = folders.listIterator();

			while (folderIter.hasNext()){
				Folder fldr = (Folder)folderIter.next();
				logger.debug("Folder: " + fldr.FolderName + ": creation started");

				sMgr.beginTransaction();

				//see if the folder exists
				IDfFolder folder = session.getFolderByPath(fldr.FolderPath + "/" + fldr.FolderName);

	            //get the acl domain
	            IDfTypedObject serverConfig = session.getServerConfig();
	            String aclDomain = serverConfig.getString("operator_name");
	            logger.debug("Folder: " + fldr.FolderName + ": acl domain: " + aclDomain);

	            //build an acl reference
	            IDfACL acl = session.getACL(aclDomain, fldr.ACL);

				if (folder == null) {
					logger.debug("Folder: " + fldr.FolderName + ": does not exist");
					folder = (IDfFolder)session.newObject(fldr.FolderType);
					folder.setObjectName(fldr.FolderName);
					folder.setACL(acl);
		            if (!fldr.FolderType.equals("dm_cabinet")) folder.link(fldr.FolderPath);

				}
	            else {
	            	logger.debug("Folder: " + fldr.FolderName + ": exists");
					folder.setObjectName(fldr.FolderName);
					folder.setACL(acl);
	            }

	            //save the folder
	            folder.save();
				logger.debug("Folder: " + fldr.FolderName + ": created");

	            //end the transaction
	            sMgr.commitTransaction();
			}

		} catch (DfException dfe){
			logger.warn(dfe.getStackTraceAsString());
			sMgr.setTransactionRollbackOnly();
		} finally {
			sMgr.release(session);
		}

	}

	public void processACLs() {
		IDfSession session = null;
		try {
			session = sMgr.getSession(docbase);

			List ACLs = dbi.ACLs;

			ListIterator aclIter = ACLs.listIterator();

			while (aclIter.hasNext()){
				ACL acl = (ACL)aclIter.next();
				logger.debug("ACL: " + acl.ACLName + ": creation started");

				sMgr.beginTransaction();

	            //get the acl domain
	            IDfTypedObject serverConfig = session.getServerConfig();
	            String aclDomain = serverConfig.getString("operator_name");

				//see if the acl exists
				IDfACL pSet = session.getACL(aclDomain, acl.ACLName);

				if (pSet == null) {
					logger.debug("ACL: " + acl.ACLName + ": does not exist");
					pSet = (IDfACL)session.newObject("dm_acl");
					pSet.setObjectName(acl.ACLName);
					pSet.setDomain(aclDomain);
				}
	            else {
	            	logger.debug("ACL: " + acl.ACLName + ": exists");
	            }

	            //set the description
				pSet.setDescription(acl.Description);

				pSet.setACLClass(0);
				pSet.save();
	            logger.debug("ACL: " + acl.ACLName + ": saved the first time");

	            //set the accessors
	            Set accessorSet = acl.Accessors.keySet();

	            Iterator accessorKeys = accessorSet.iterator();

	            while (accessorKeys.hasNext()){
	            	String accessor = (String)accessorKeys.next();
	            	logger.debug("ACL: " + acl.ACLName + ": accessor: " + accessor);
	            	AccessRights ar = (AccessRights)acl.Accessors.get(accessor);
	            	logger.debug("ACL: " + acl.ACLName + ": permit: " + ar.Permit);

	            	String xPermits = null;
	            	Iterator xPermitIter = ar.ExtendedPermits.iterator();

	            	if (!ar.ExtendedPermits.isEmpty()) xPermits = (String)xPermitIter.next();
	            	while (xPermitIter.hasNext()){
	            		xPermits += "," + (String)xPermitIter.next();
	            	}
	            	logger.debug("ACL: " + acl.ACLName + ": xpermits: " + xPermits);
	            	
	            	//pSet.grant(accessor,processAccess(ar.Permit),xPermits);
	            	//pSet.revoke(accessor,"EXECUTE_PROC,CHANGE_LOCATION,CHANGE_STATE,CHANGE_PERMIT,CHANGE_OWNER");
	            	pSet.grant(accessor,processAccess(ar.Permit),xPermits);
	            	//if (!ar.ExtendedPermits.contains("EXECUTE_PROC")) pSet.revoke(accessor,"EXECUTE_PROC");
	            	//if (!ar.ExtendedPermits.contains("CHANGE_LOCATION")) pSet.revoke(accessor,"CHANGE_LOCATION");
	            	
	            	logger.debug("ACL: " + acl.ACLName + ": accessor: " + accessor + " granted permissions");
	            }

				pSet.save();
	            logger.debug("ACL: " + acl.ACLName + ": saved the last time");

	            //end the transaction
	            sMgr.commitTransaction();
			}

		} catch (DfException dfe){
			logger.warn(dfe.getStackTraceAsString());
			sMgr.setTransactionRollbackOnly();
		} finally {
			sMgr.release(session);
		}

	}

	public void processObjects() {
		IDfSession session = null;
		try {
			session = sMgr.getSession(docbase);

			List objects = dbi.DocbaseObjects;

			ListIterator objIter = objects.listIterator();

			while (objIter.hasNext()){
				DocbaseObject dbo = (DocbaseObject)objIter.next();

				logger.debug(dbo.ObjectName + ": creation started");

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
	            	logger.debug(dbo.ObjectName + ": exists");
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

	            	logger.debug(dbo.ObjectName + ": property: " + propertyName);
	            	PropertyValue pv = (PropertyValue)dbo.Properties.get(propertyName);
	            	logger.debug(dbo.ObjectName + ": repeating: " + pv.Repeating);
	            	boolean isRepeating = Boolean.valueOf(pv.Repeating).booleanValue();
	            	if (isRepeating) {
	            		dbObject.removeAll(propertyName); // CEM - need to clear list so old values don't stick around...	            		
	            	}
	            	Iterator propertyValues = pv.Values.iterator();
	            	while (propertyValues.hasNext()){
	              		String value = (String)propertyValues.next();
	      		       	if (!isRepeating) {
		            		dbObject.setString(propertyName,value);
			            	logger.debug(dbo.ObjectName + ": single valued property: " + value);
		            	}
		            	else {
		            		dbObject.appendString(propertyName,value);
			            	logger.debug(dbo.ObjectName + ": repeating property: " + value);
		            	}
	            	}

	            }

	            //save the object
	            dbObject.save();

	            logger.debug(dbo.ObjectName + ": saved");

	            //end the transaction
	            sMgr.commitTransaction();

			}

		} catch (DfException dfe){
			logger.warn(dfe.getStackTraceAsString());
			sMgr.setTransactionRollbackOnly();
		} finally {
			sMgr.release(session);
		}

	}



	private void setSessionMgr(){

		try {
	        IDfClientX clientx = new DfClientX();

	       	IDfClient client = clientx.getLocalClient();

	       	sMgr = client.newSessionManager();

	       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
	        loginInfoObj.setUser(username);
	        loginInfoObj.setPassword(password);
	        loginInfoObj.setDomain(null);

	        sMgr.setIdentity(docbase, loginInfoObj);
        }
        catch (DfException dfe){
        	dfe.printStackTrace();
        }
	}

	private int processAccess(String permit){
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

}

