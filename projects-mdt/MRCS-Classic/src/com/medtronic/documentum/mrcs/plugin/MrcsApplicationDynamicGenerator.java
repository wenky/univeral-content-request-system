package com.medtronic.documentum.mrcs.plugin;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPermit;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfList;
import com.medtronic.documentum.mrcs.config.MrcsApplication;
import com.medtronic.documentum.mrcs.config.MrcsConfigBroker;
import com.medtronic.documentum.mrcs.config.MrcsGroupingFolderType;
import com.thoughtworks.xstream.XStream;

/*
 * configuration of this class consists of a set of ACLs to clone, a set of groups to create, a and config template to clone and process
 * ACLs - list of strings of ACLs to clone
 * Template - location of config template
 * ConfigDirectory - where to put the successfully processed config
 * TODO - group creation...
 */

public class MrcsApplicationDynamicGenerator implements MrcsFolderCreationPlugin 
{
    public void processFolder(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception
    {
    	HashMap submatrix = new HashMap();
    	// get name - based off of the generated grouping folder name
    	IDfFolder newgf = (IDfFolder)session.getObject(new DfId(objectid));
    	String foldername = newgf.getObjectName();
    	String newappname = "newapp-"+foldername;
    	submatrix.put("appname",newappname);
    	// clone ACLs - a list of properties under:
    	//  - <string>ACLs</string><list><string>aclname1</string><string>aclname2</string></list>
    	List aclnames = (List)configdata.get("ACLs");
    	for (int i = 0; i < aclnames.size(); i++)
    	{
    		String aclname = (String)aclnames.get(i);
    		IDfACL baseacl = (IDfACL)session.getObjectByQualification("dm_acl where object_name = '"+aclname+"'");
    		IDfACL newacl = cloneACL(session,baseacl,foldername+'-'+aclname);
    		submatrix.put(aclname,foldername+'-'+aclname);
    	}
    	// clone groups
    	// clone config

    	// need this for stream conversion
        IDfClientX clientx = new DfClientX();

    	// process template
    	String templatename = (String)configdata.get("Template");
        IDfSysObject sysObj = (IDfSysObject)session.getObjectByPath(templatename);
        
        ByteArrayInputStream bais = sysObj.getContent();
        String configtemplate = null;
        if (bais.available() > 0)
        {
        	configtemplate = clientx.ByteArrayInputStreamToString(bais);
        }
        
        // poor man's velocity substitution
        Iterator iter = submatrix.keySet().iterator();
        while (iter.hasNext())
        {
        	String key = (String)iter.next();
        	String value = (String)submatrix.get(key);
        	configtemplate = configtemplate.replaceAll("\\$"+key,value);
        }
        
        // get prepped XStream
        MrcsConfigBroker configbroker = MrcsConfigBroker.getConfigBroker();
        XStream xs = configbroker.getPreppedXStreamFromDCTM();
        
        MrcsApplication mrcsapplication = null;
        // try deserialization
        try {
        	mrcsapplication = (MrcsApplication)xs.fromXML(configtemplate);
        } catch (Exception e) {
        	throw (e);
        }
        
        // TODO switch the base ACL of the grouping folder to a presumably newly cloned ACL..
        MrcsGroupingFolderType gf = (MrcsGroupingFolderType)mrcsapplication.GroupingFolderTypes.get(newgf.getString("mrcs_config"));
        String aclqual = "dm_acl where object_name = '"+gf.GroupingFolderACL+"'";
		IDfACL newgfacl = (IDfACL)session.getObjectByQualification(aclqual);
        //TODO figure out bug: newgf.setACL(newgfacl);

    	// create in docbase
        String configlocation = (String)configdata.get("ConfigDirectory"); // /System/Applications/mrcs_core/
        IDfDocument newconfigObj = (IDfDocument)session.newObject("dm_document");
        newconfigObj.setContentType("crtext");
        newconfigObj.setContent(clientx.StringToByteArrayOutputStream(configtemplate));
        newconfigObj.setObjectName("mrcsapp_"+foldername+"_generated.xml");
        newconfigObj.link(configlocation); // hmmm, this may require a switch to system account...
        newconfigObj.save();
        
        // REVISE gf's mrcs_application attribute
        newgf.setString("mrcs_application",newappname);
        newgf.save();

    	// append new mrcsapp to factory...
        configbroker.addMrcsApplication(mrcsapplication);

    }
    
    public IDfGroup cloneGroup(IDfSession session, IDfUser currentuser, String name) throws Exception
    {
    	IDfGroup newgrp = (IDfGroup)session.newObject("dm_group");
    	newgrp.setString("object_name",name);
    	newgrp.addUser(currentuser.getUserName());
    	newgrp.save();
    	return newgrp;
    }
    
    public IDfACL cloneACL(IDfSession session, IDfACL baseacl, String name) throws Exception
    {
    	//create,c,dm_acl
    	IDfACL newacl = (IDfACL)session.newObject("dm_acl");
    	//set,c,l,object_name	World Write
    	newacl.setString("object_name",name);
    	//set,c,l,owner_name dm_dbo
    	newacl.setDomain(null);
    	//set,c,l,description All Users have Write Access
    	newacl.setString("description","dynamically generated acl for MRCS-Japan");
    	
    	//clone access privledges
    	//grant,c,l,dm_world,6 
    	//grant,c,l,dm_owner,7
    	//grant,c,l,system_admins,7
    	IDfList permissions = baseacl.getPermissions();
    	int size = permissions.getCount();
    	for (int i=0; i < size; i++)
    	{
    		IDfPermit permit = (IDfPermit)permissions.get(i);
    		newacl.grantPermit(permit);
    	}
    	//save,c,l
    	newacl.save();
    	
    	return newacl;
    }
	

}
