package com.medtronic.ecm.documentum.qad.reassigntask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;

/**
 * Get logged in user
 * Get the application user belongs to   
 * Read the XML Config file and get the group names and get users.  
 * @author molakr1
 *
 */
public class MdtReassignReadConfigValues {

	/**
	 * Get the information about log in user like belongs to which application
	 * Read the group names from config  
	 * rerun list of users in that groups
	 * @param session
	 * @return List of user
	 * @throws DfException
	 */
	public List getUserNamesFromConfig(IDfSession session) throws DfException
	{
        /*-INFO-*/Lg.inf("top");            	    
		List m_UserMap 		= null;
		String strUserName 	= null;
		
		// get configuration
        /*-INFO-*/Lg.inf("get mdt config service");            
		MdtConfigService configsvc = MdtConfigService.getConfigService(session.getSessionManager(), session.getDocbaseName());  // <-- STEP 1	
		
		//Get Login user name
		String user = session.getLoginUserName();
		/*-dbg-*/Lg.dbg("current loginuser %s",user);

		//Get teh application name 
		String  mdtapp = (String)configsvc.getMdtApplicationsFromGroupMembership(user).get(0);
		/*-dbg-*/Lg.dbg("users mdt application  %s",mdtapp);
		
		//Read the config and get the assignable groups
		// TODO: ?filter this later based on the selected workitem?
		Map config = (Map)configsvc.getAppConfig(mdtapp);
        /*-dbg-*/Lg.dbg("get list of users/groups that can be reassigned to %s",mdtapp);
		List reassignmentgroups = (List)config.get("TaskReassignment");
		
		m_UserMap = new ArrayList();
		//Get the users in groups and add to List
		for (int i=0; i < reassignmentgroups.size(); i++)
		{
			String grpname = (String)reassignmentgroups.get(i);
			/*-dbg-*/Lg.dbg("Group name  %s",grpname);
			IDfGroup group = session.getGroup(grpname);
			//Get collection of users in a group 
			// and iterate thru and add to list
			IDfCollection users = group.getUsersNames();
			while (users.next()) {
				String arrUser[] = new String[1];
				strUserName = users.getString("users_names");
				arrUser[0] = strUserName;
				m_UserMap.add(arrUser);
			}
			users.close();
		}
		return m_UserMap;
	}
	
	/**
	 * Called from Reassign precondition class
	 * Read the config and get groups in TaskReassignmentAdmin
	 * And checks is logged in user in one of the groups and return boolean.
	 * @param session
	 * @return boolean
	 * @throws DfException
	 */
	public boolean isUserExistsInTaskReassignmentAdminGroup(IDfSession session) throws DfException{
		List m_UserMap 		= null;
		String strUserName 	= null;
        /*-INFO-*/Lg.inf("check if current user is in one of the reassign admin groups");
		boolean isUserExistsInGroup = false;
		// get configuration
		MdtConfigService configsvc = MdtConfigService.getConfigService(session.getSessionManager(), session.getDocbaseName());  // <-- STEP 1	
		
		//Get Login user name
		String user = session.getLoginUserName();
		/*-dbg-*/Lg.dbg("logged in user  %s",user);

		//Get teh application name 
		String  mdtapp = (String )configsvc.getMdtApplicationsFromGroupMembership(user).get(0);
		/*-dbg-*/Lg.dbg("mdt application  %s",mdtapp);
		
		//Read the config and get the groups
		Map config = (Map)configsvc.getAppConfig(mdtapp);
		List reassignmentgroups = (List)config.get("TaskReassignmentAdmin");
		
		m_UserMap = new ArrayList();
		//Get the users in groups and add to List
		for (int i=0; i < reassignmentgroups.size(); i++) {
			String grpname = (String)reassignmentgroups.get(i);
			/*-dbg-*/Lg.dbg("Group name  %s",grpname);
			IDfGroup group = session.getGroup(grpname);
			isUserExistsInGroup = group.isUserInGroup(user);
			//If true come out of the loop and return true.
			if(isUserExistsInGroup == true) {
				return isUserExistsInGroup;
			}
		}
		return isUserExistsInGroup;
	}
	
}
