/*
 * Created on Jan 25, 2005
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

 Filename       $RCSfile: REFactory.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:02 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.util;


import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class REFactory {
    
    /*
     * this is for interceptings RESyntaxExceptions -- an unnecessary pain in the tush
     */
    
    public static RE createRE(String regexp)
    {
        try {
            return new RE(regexp);            
        } catch (RESyntaxException rese)
        {
            // at least log the failed compilation!
            String[] errorparams = new String[1]; errorparams[0] = regexp;
            /*-ERROR-*/DfLogger.error(null,"MRCS_REFACTORY_COMPILE_ERROR",errorparams,rese);
            return null;
        }
    }

}
