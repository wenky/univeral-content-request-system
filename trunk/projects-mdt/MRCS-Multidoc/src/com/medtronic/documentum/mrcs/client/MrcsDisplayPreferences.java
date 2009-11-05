
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.

***********************************************************************

 Project        MRCS
 Version        4.2.2
 Description
 Created on		December 6, 2007

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: MrcsDisplayPreferences.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2007/12/12 17:39:32 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;

import com.medtronic.documentum.mrcs.config.*;

import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfException;
import com.documentum.webcomponent.environment.preferences.display.ColumnSelector;
import com.documentum.web.common.ArgumentList;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.com.IDfClientX;
import com.documentum.com.DfClientX;

import java.util.*;

/**
 * @author hansom5
 *
 * this overrides the initializer for the display column preferences
 *
 */
public class MrcsDisplayPreferences extends ColumnSelector
{

	private TreeSet typeSet;

    public void onInit(ArgumentList argumentlist)
    {
        /*-CONFIG-*/String m="onInit - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" start init." , null, null);

        String strDocbaseTypes = argumentlist.get("docbaseTypes");

		/* Method for fetching docbase types from installed configs */
		setTypeSet();
		strDocbaseTypes += getTypeList();

		/* Method for querying docbase types based on values seeded in mrcs_display_preferences_component.xml */
		//strDocbaseTypes += parseTypes(strDocbaseTypes);

		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" attribute selector docbase types: " + strDocbaseTypes, null, null);

        argumentlist.replace("docbaseTypes", strDocbaseTypes);

        super.onInit(argumentlist);
    }

	private String parseTypes(String argTypes){
		String returnList = "";
		for (int i = 0; i < argTypes.length(); i += argTypes.indexOf("*",i)) {
			String checkType = argTypes.substring(i, argTypes.indexOf("*",i));
			if (!checkType.equals("")) {
				returnList += getTypeList(checkType);
				/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"parseTypes() querying type:  " + checkType, null, null);
			}
		}
		return returnList;
	}

	private String getTypeList(String type) {
		String types = "";
		try {
			IDfClientX clientx = new DfClientX();
			IDfQuery typeQ = clientx.getQuery();
			typeQ.setDQL("select name from dm_type where super_name = '" + type + "'");
			IDfCollection typeCol = typeQ.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
			while (typeCol.next()){
				IDfType objType = getDfSession().getType(typeCol.getString("name"));
				types += "*";
				types += objType.getName();
				types += "*";
				types += objType.getDescription();
			}
		} catch (DfException dfe) {
			/*-ERROR-*/DfLogger.error(this,"There was an error getting the docbase types by query.",null,dfe);
		}
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"getTypeList() return:  " + types, null, null);
		return types;
	}

	private String getTypeList(){
		String types = "";
		Iterator iterTypeSet = typeSet.iterator();
		try {
			while (iterTypeSet.hasNext()){
				String typeName = (String)iterTypeSet.next();
				IDfType objType = getDfSession().getType(typeName);
				types += "*";
				types += objType.getName();
				types += "*";
				types += objType.getDescription();
			}
		} catch (DfException dfe) {
			/*-ERROR-*/DfLogger.error(this,"There was an error getting the docbase types from the configs.",null,dfe);
		}
		return types;
	}

	private void setTypeSet(){
		typeSet = new TreeSet();
		MrcsConfigBroker docConfig = MrcsConfigBroker.getConfigBroker();
		HashMap appMap = docConfig.getApplications();
		Iterator iterApp = appMap.keySet().iterator();
		while (iterApp.hasNext()){
			MrcsApplication app = (MrcsApplication)appMap.get(iterApp.next());
			Map docTypes = (Map)app.DocumentTypes;
			Iterator iterType = docTypes.keySet().iterator();
			while (iterType.hasNext()){
				MrcsDocumentType type = (MrcsDocumentType)docTypes.get(iterType.next());
				typeSet.add(type.DocumentumSystemType);
			}
		}
	}

	private void getTypeSet(){
		Iterator iterTypeSet = typeSet.iterator();
		while (iterTypeSet.hasNext()){
			/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"getting system type: " + (String)iterTypeSet.next(), null, null);
		}
	}

}
