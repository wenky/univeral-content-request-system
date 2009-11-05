package com.medtronic.ecm.documentum.core.plugins.naming;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.5 $
 * 
 */ 
public class MdtUserSpecifiedNaming implements IMdtDocumentNaming 
{
	
	/**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param docbase <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param object <font color="#0000FF"><b>(IDfSysObject)</b></font> TODO:
    * @param context <font color="#0000FF"><b>(Object)</b></font> TODO:
    * @return <font color="#0000FF"><b>String</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
    public String generateName(String docbase, IDfSysObject object, Map context)
    {
        /*-INFO-*/Lg.inf("top of plugin");
        String userspecified = "";
        try { 
            /*-dbg-*/Lg.dbg("get object name");
        	userspecified = object.getObjectName();
            /*-dbg-*/Lg.dbg("object name: %s",userspecified);
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("DFE trying to get user specified object name component",dfe);
            throw EEx.create("NAME-UserSpec-DFE", "Unable to get the user specified name component",dfe);
        }
        return userspecified;
    	
    }

}
