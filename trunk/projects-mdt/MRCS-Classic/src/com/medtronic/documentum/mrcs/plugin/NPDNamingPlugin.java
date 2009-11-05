/*
 * Created on March 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: NPDNamingPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:58 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;


/**
 * @author hansom5
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NPDNamingPlugin implements MrcsNamingFormatPlugin, MrcsDocumentNamingFormatPlugin {

	private static int folderNumber = 1;

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.util.MrcsNamingFormatPlugin#generateName(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    // MrcsNamingFormatPlugin method
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid,
            Map configdata, Map customdata) throws Exception {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:NPDNamingPlugin.generateName - top of plugin",null,null);
        String objectname = (String)customdata.get("ObjectName");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:NPDNamingPlugin.generateName - scraped object name: "+objectname,null,null);
        // get the foldername from customdata

        return formatFolderNumber() + " " + objectname;
    }

    // MrcsDocumentNamingFormatPlugin method
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String objectid, Map configdata, Map customdata) throws Exception
    {
        String name = generateName(session,mrcsapp,gftype,objectid,configdata,customdata);
        return name;
    }

    private static int getNextNumber()
    {
		return folderNumber++;
	}

	private String formatFolderNumber()
	{
		return "NDHF" + padNumber(String.valueOf(NPDNamingPlugin.getNextNumber()),"1000");
	}

	private String padNumber(String number, String name)
	{
		String paddedNumber = null;
		int i = number.length();
		System.out.println("number.length: " + i);
		int j = name.length();
		System.out.println("name.length(-1): " + j);
		int k = j - i;
		System.out.println("name - number length: " + k);
		paddedNumber = name.substring(0, k);
		System.out.println("name.substring(0, k): " + paddedNumber);
		paddedNumber = paddedNumber + number;
		System.out.println("paddedNumber: " + paddedNumber);
		return paddedNumber;
	}


}
