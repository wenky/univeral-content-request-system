/*
 * Created on Feb 3, 2005
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

 Filename       $RCSfile: RandomNumberName.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:58 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;
import java.util.Random;

import com.documentum.fc.client.IDfSession;


/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RandomNumberName implements MrcsNamingFormatPlugin, MrcsDocumentNamingFormatPlugin {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.util.MrcsNamingFormatPlugin#generateName(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    static Random rng; 
    
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid,
            Map configdata, Map customdata) throws Exception {
        // TODO Auto-generated method stub
        synchronized (rng)
        {
            if (rng == null)
                rng = new Random(System.currentTimeMillis());
        }
        return ""+rng.nextInt()+"-"+System.currentTimeMillis();
    }

    // MrcsDocumentNamingFormatPlugin method
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String objectid, Map configdata, Map customdata) throws Exception
    {
        String name = generateName(session,mrcsapp,gftype,objectid,configdata,customdata);
        return name;
    }

}
