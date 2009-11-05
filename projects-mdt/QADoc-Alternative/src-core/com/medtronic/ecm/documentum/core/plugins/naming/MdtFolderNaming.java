package com.medtronic.ecm.documentum.core.plugins.naming;

import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.6 $
 *  
 */  
public class MdtFolderNaming implements IMdtDocumentNaming 
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
        try { 
            /*-INFO-*/Lg.inf("current folder name plugin called");
            IDfFolder folder = (IDfFolder)object.getSession().getObject(object.getFolderId(0));
            String foldername = folder.getObjectName();
            /*-dbg-*/Lg.dbg("folder name to append: %s",foldername);
            return foldername;
         } catch (DfException dfe) {
            // check df exception so we know if a problem occurred before we even hit the exception/checkout loop
            /*-ERROR-*/Lg.err("DFException in getting folder name",dfe);
            throw EEx.create("NAME-FldNm-DFE", "DFException in getting folder name",dfe);
         }
    }
    
}
