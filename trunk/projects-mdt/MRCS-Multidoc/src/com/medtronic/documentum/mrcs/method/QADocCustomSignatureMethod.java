package com.medtronic.documentum.mrcs.method;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import com.documentum.fc.common.DfLogger;
import com.documentum.mthdservlet.IDmMethod;

public class QADocCustomSignatureMethod extends MrcsConfigurableMethod implements IDmMethod
{
    public void execute(Map parameters, OutputStream output) throws Exception
    {
       	/*-CONFIG-*/String m="QADocCustomSignatureMethod.execute - ";
    	// what is it passing us for arguments?
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))try {Iterator i = parameters.keySet().iterator(); while (i.hasNext()){DfLogger.debug(this, m+"paramkey: "+(String)i.next() , null, null);}} catch (Exception e) {}

       	// that's all for now!
    }

}
