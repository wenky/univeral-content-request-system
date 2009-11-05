/*
 * Created on Aug 7, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfAliasSet;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MSDCASValidateReleaseDates implements IMrcsWorkflowValidation
{
    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m="validate-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of MSD CAS validate release dates", null, null);

        boolean valid = true;
        // check that the Required By attribute (msd_cas_job_completion_date) is filled
        String requiredby = doc.getString("msd_cas_job_completion_date");
        
        if (requiredby == null || "".equals(requiredby.trim()) || "nulldate".equals(requiredby))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"it's empty, validation failed", null, null);
            Map error = new HashMap();
            error.put("Error","ERR_VALIDATION_MSD_CAS_REQUIREDBY_MISSING");
            Object[] params = {"msd_cas_job_completion_date"};
            error.put("Params",params);
            errmsgs.add(error);                            
            valid = false;
            return valid;            
        }

        // if we're here, it's populated. see if release/dissemination date is empty
        String releaseby = doc.getString("msd_cas_dissemination_date");
        
        if (releaseby == null || "".equals(releaseby.trim()) || "nulldate".equals(releaseby))
        {
            // set the attr to the requiredby date... (do we need to switch to sysuser?)
            doc.setTime("msd_cas_dissemination_date",doc.getTime("msd_cas_job_completion_date"));
            doc.save();
            valid = true;
        } else {
            // check if dissemination is before completion date
            if (doc.getTime("msd_cas_job_completion_date").compareTo(doc.getTime("msd_cas_dissemination_date")) > 0)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dissemination date is earlier than distribution date...validation failed", null, null);
                Map error = new HashMap();
                error.put("Error","ERR_VALIDATION_DISSEMINATION");
                Object[] params = {};
                error.put("Params",params);
                errmsgs.add(error);                            
                valid = false;
                return valid;            
                
            } else {
                valid = true;
            }
        }
        return valid;
    }
}
