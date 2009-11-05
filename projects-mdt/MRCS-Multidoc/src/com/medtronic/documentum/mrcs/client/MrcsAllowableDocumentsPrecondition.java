/*
 * Created on Nov 1, 2005
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

 Filename       $RCSfile: MrcsAllowableDocumentsPrecondition.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:42 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.List;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;


/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsAllowableDocumentsPrecondition implements IActionPrecondition
{

    public String[] getRequiredParams() {
        return (new String[] { "objectId" });
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
        // get from config XML for this action, ie: <precondition class="blah"><role>ARole</role></precondition>
        //String configrole = iconfigelement.getChildValue("role");  
        /*-CONFIG-*/String m="queryExecute";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - checking for allowable docs in mrcs folder config",null,null);

        // get foldre id
        String folderid = argumentlist.get("objectId");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - folder id: "+folderid,null,null);
        // look up folder
        IDfSession idfsession = component.getDfSession();
            
        // see if this folder's gf has allowable documents
        try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - looking up folder by id",null,null);
            IDfFolder folder = (IDfFolder)idfsession.getObject(new DfId(folderid));

            // get mrcs_app and mrcs_gf_type from folder
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - looking up mrcs attributes",null,null);
            String mrcsapp = folder.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -   -->app: "+mrcsapp,null,null);
            String gftype = folder.getString("mrcs_config");
            MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" -   -->gftype: "+gftype,null,null);
            try { 
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - getting list of allowable documents for this gftype",null,null);
                List doclist = docconfig.getAllowableDocumentTypes(mrcsapp, gftype);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - check if the list has at least one document",null,null);
                if (doclist != null && doclist.size() > 0) 
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - at least one allowable document registered, returning true",null,null);
                    return true;
                }
            } catch (NullPointerException npe) {
                // need to change this, but for now:
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - npe thrown, which means no allowable docs were found, returning false",null,null);
                return false;
            }
        } catch (Exception e) {            
            /*-ERROR-*/DfLogger.error(this,m+" - unexpected exception while checking for allowable documents",null,e);
            throw new RuntimeException(e);
        }
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - no allowable docs detected, returning false",null,null);
        return false;
    }
        
}
