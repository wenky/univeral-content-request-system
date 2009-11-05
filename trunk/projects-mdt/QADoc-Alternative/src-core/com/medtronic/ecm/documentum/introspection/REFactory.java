package com.medtronic.ecm.documentum.introspection;


import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class REFactory {
    
    /*
     * this is for interceptings RESyntaxExceptions -- an unnecessary pain in the tush
     */
    
    public static RE createRE(String regexp)
    {
        try {
            return new RE(regexp);            
        } catch (RESyntaxException rese)
        {
            // at least log the failed compilation!
            String[] errorparams = new String[1]; errorparams[0] = regexp;
            /*-ERROR-*/DfLogger.error(null,"MRCS_REFACTORY_COMPILE_ERROR",errorparams,rese);
            return null;
        }
    }

}
