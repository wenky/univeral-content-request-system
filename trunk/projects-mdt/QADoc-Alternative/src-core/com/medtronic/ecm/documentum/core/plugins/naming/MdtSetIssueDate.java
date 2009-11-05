package com.medtronic.ecm.documentum.core.plugins.naming;

import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;

public class MdtSetIssueDate implements IMdtDocumentNaming 
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
        /*-INFO-*/Lg.inf("set issue date called");
        String attrname = (String)context.get("AttrName");
        /*-dbg-*/Lg.dbg("set: %s to current date",attrname);
        try {
            object.setTime(attrname, new DfTime(new Date()));
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("DFException in setting attr %s to current date",attrname,dfe);
            throw EEx.create("NAME-SetDate-DFE", "DFException in setting attr %s to current date",attrname,dfe);
            
        }
        return "";
    }

}
