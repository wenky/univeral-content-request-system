package com.medtronic.ecm.documentum.core.plugins.naming;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.6 $
 *  
 */  
public class MdtConstantNaming implements IMdtDocumentNaming 
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
        /*-INFO-*/Lg.inf("constant/static name plugin called");
        String staticname = (String)context.get("Name");
        /*-dbg-*/Lg.dbg("static name to append: %s",staticname);
        return staticname;
    }

}
