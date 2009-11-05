/*
 * Created on Oct 12, 2005
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

 Filename       $RCSfile: SetAllObsolete.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/20 19:13:25 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author muellc4
 * 
 * This sets every other document, regardless of state, to the specified LC and state,
 * which must be marked directly assignable in the DCTM AppBuilder for the LC's state.
 * Note that you cannot directly attach to a suspend/exception state, you'll have to write
 * a plugin that attaches to that state and then suspends it...
 * - process() of ThereCanOnlyBeOne is reused
 */
public class SetAllObsolete extends ThereCanOnlyBeOne
{
    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "SetAllObsolete.getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id", null, null);
        String docid = PostVersionedDoc.getObjectId().getId();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up both newer and older versions of the current doc", null, null);
        String qualification = "select r_object_id from dm_sysobject (ALL) "+
                               "where NOT r_object_id = '"+docid+"' AND i_chronicle_id in " +
                                    "(select i_chronicle_id from dm_sysobject where r_object_id = '"+docid + "')";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }

}
