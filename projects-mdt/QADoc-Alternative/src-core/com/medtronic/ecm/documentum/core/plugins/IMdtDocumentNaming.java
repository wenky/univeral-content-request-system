package com.medtronic.ecm.documentum.core.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.1 $
 * 
 */ 
public interface IMdtDocumentNaming
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
    public String generateName(String docbase, IDfSysObject object, Map context);
}
