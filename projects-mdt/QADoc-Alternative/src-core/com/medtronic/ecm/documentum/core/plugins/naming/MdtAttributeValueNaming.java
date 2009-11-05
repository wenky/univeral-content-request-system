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
 * @version $Revision: 1.6 $
 *  
 */ 
public class MdtAttributeValueNaming implements IMdtDocumentNaming 
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
    public String generateName(String docbase, IDfSysObject object, Map configdata)
    {
        /*-INFO-*/Lg.inf("constant/static name plugin called");
        String attrname = (String)configdata.get("Name");
        /*-dbg-*/Lg.dbg("get value of attr: %s",attrname);
        String value = "";
        try { 
        	value = object.getValue(attrname).asString();
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("DFException in attribute lookup",dfe);
            throw EEx.create("NAME-Attr", "DFException in attribute lookup",dfe);        	
        }
        /*-dbg-*/Lg.dbg("value: %s",value);
        String prepend = (String)configdata.get("Prepend");
        if (prepend != null) value = prepend+value;
        String append = (String)configdata.get("Append");
        if (append != null) value = value+append;

        /*-dbg-*/Lg.dbg("postprocess value: %s",value);

        return value;
        
    }


}
