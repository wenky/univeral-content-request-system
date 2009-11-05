package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.io.OutputStream;
import java.util.Map;

import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.ecm.common.Lg;

// make sure this is synchronous, 

public class MdtPing implements IDmMethod
{
    
    public void execute(Map parameters, OutputStream outputstream) throws Exception
    {
        try { 
            /*-INFO-*/Lg.inf("MdtPing invoked");
            /*-SYSOUT-*/System.out.println("MdtPing invoked");
            String success = "Ping-ACK";
            if (outputstream != null) {
                outputstream.write(success.getBytes());
                outputstream.flush();
                /*-INFO-*/Lg.inf("Ping-ACK");
                /*-SYSOUT-*/System.out.println("Ping-ACK");
            } else {
                /*-WARN-*/Lg.wrn("outputstream is null, need to make MdtPing method a synchronous call");
                /*-SYSOUT-*/System.out.println("outputstream is null, need to make MdtPing method a synchronous call");            
            }
        } catch (Exception e) {
            try {/*-ERROR-*/Lg.err("MdtPing error",e);} catch (Throwable t) {}
            try {/*-SYSOUT-*/System.out.println("MdtPing error "+e.toString());e.printStackTrace();} catch (Throwable t) {}
        }
    }
}
