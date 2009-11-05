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

 Filename       $RCSfile: DeleteAllCopies.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/20 19:13:25 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author muellc4
 *
 * deletes all copies created by the CopyTo plugins, or any document with a relation
 * to the current document or any previous version. Can be used, for example, to clean
 * away copies along with the SetAllObsolete plugin 
 */
public class DeleteAllCopies extends ThereCanOnlyBeOne
{

    // this looks up the list of all document copies via relationships in dm_relation... 
    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id", null, null);
        String docid = PostVersionedDoc.getObjectId().getId();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up related copies to the current doc and its previous version", null, null);
        String qualification = "select child_id from dm_relation where parent_id in " +
                                "(select r_object_id from dm_sysobject (ALL) where i_chronicle_id in " +
                                "(select i_chronicle_id from dm_sysobject where r_object_id = '" +docid+"')) ";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }
    
    // this deletes document matches!
    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "process()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
                
        // extract document id from current match (assumes docid is the only string in the match's attribute colleciton), lookup doc in docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get match's object id", null, null);
        String id = getMatchObjectId(match);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up document", null, null);
        IDfDocument doc = (IDfDocument)session.getObject(new DfId(id));
        // make object mutable, and attach the new lifecycle (policy)
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"begin attach and detach of new lifecycle", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"make document mutable", null, null);
        // user should be SUPERUSER in order to play with r_immutable_flag
        doc.setString("r_immutable_flag", "FALSE");
        doc.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"DELETE/destroy the document", null, null);
        doc.destroy();
        //doc.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);
    }



}
