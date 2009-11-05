/*
 * Created on Nov 3, 2005
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

 Filename       $RCSfile: ValidateRequiredAttributes.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/12/18 21:28:47 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.WrapperRuntimeException;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ValidateRequiredAttributes implements IMrcsWorkflowValidation
{

    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        // iterate through configured attributes
        /*-CONFIG-*/String m="ValidateRequiredAttributes.validate - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Begin required attribute validation", null, null);
        
        // get column names for current locale
    	// DMCL trace for DDL lookup...need to get locale...
    	// SELECT DISTINCT label_text,attr_name,LOWER(label_text) FROM dmi_dd_attr_info WHERE type_name = 'm_mrcs_crmsdo_contract' AND nls_key = 'en' ORDER BY 3
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DECODING attribute labels",null,null);
    	String locale = LocaleService.getLocale().getLanguage();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"locale: "+locale, null, null);
    	String doctype = doc.getTypeName();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype: "+doctype, null, null);
        DfQuery dfquery = new DfQuery();
        String labelquery = "SELECT DISTINCT label_text,attr_name,LOWER(label_text) FROM dmi_dd_attr_info WHERE type_name = '"+doctype+"' AND nls_key = '"+locale+"' ORDER BY 3";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Setting query: "+labelquery, null, null);
        dfquery.setDQL(labelquery);
        
        HashMap labelmap = new HashMap();
    	
        IDfCollection idfcollection = null;
        try
        {
            idfcollection = dfquery.execute(doc.getSession(), 0);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Iterating through DQL-retrieved labels", null, null);
            while (idfcollection.next())             
            {
                String attrname = idfcollection.getString("attr_name");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"ATTRIBUTE: "+attrname, null, null);
                String label = idfcollection.getString("label_text");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"  -label : "+label, null, null);
                labelmap.put(attrname,label);
            }

        }
        catch(DfException dfexception)
        {
            /*-ERROR-*/DfLogger.error(this, m+"ATTRIBUTE label query threw error", null, dfexception);
            throw new WrapperRuntimeException("Unable to query labels from docbase!", dfexception);
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                    idfcollection.close();
            }
            catch(DfException dfexception1) { }
        }
        
        int count = 1;
        boolean valid = true;
        while (true)
        {
            //get next attribute from config
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Looking up config key Required-"+count, null, null);
            String attr = (String)configdata.get("Required-"+count);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attribute to check: "+attr, null, null);
            count++;
            if (attr == null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attr name is null, returning", null, null);
                break; // exit our "infinite" loop         
            }
                        
            String value = doc.getString(attr);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking value "+value, null, null);
            if (value == null || "".equals(value.trim()) || "nulldate".equals(value))
            {            	
            	
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"it's empty, validation failed", null, null);
                Map error = new HashMap();
                error.put("Error","ERR_VALIDATION_REQUIRED_ATTRIBUTE_MISSING");
                Object[] params = {labelmap.get(attr)};
                error.put("Params",params);
                errmsgs.add(error);                            
                valid = false;
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning req'd attr validation result: "+valid, null, null);
        return valid ;
    }

    
}
