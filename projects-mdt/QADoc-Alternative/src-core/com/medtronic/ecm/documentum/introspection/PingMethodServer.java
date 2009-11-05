package com.medtronic.ecm.documentum.introspection;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfValue;

public class PingMethodServer 
{
    public String ping(String u, String p, String d, String methodname)
    {
        
        try {
            IDfSessionManager sMgr = null;
            if (sMgr == null) {
                IDfClientX clientx = new DfClientX(); 
                IDfClient client = clientx.getLocalClient();
                sMgr = client.newSessionManager();
                IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
                loginInfoObj.setUser(u);
                loginInfoObj.setPassword(p);
                loginInfoObj.setDomain(null);
                sMgr.setIdentity(d, loginInfoObj);
            }
            
            IDfSession session = sMgr.getSession(d);
            
            String callmethodapiargs =  "-docbase_name "+d;

            String args[] = {"SAVE_RESULTS", "ARGUMENTS", "METHOD", "TIME_OUT", "RUN_AS_SERVER"};
            String datatypes[] = {"B", "S", "S", "I", "B"};
            String values[] = {"F", callmethodapiargs, methodname, "800", "T"};

            IDfCollection result = session.apply(null, "DO_METHOD", new DfList(args), new DfList(datatypes), new DfList(values));
            
            String response = "";
            
            while (result.next()) {
                for (int i=0; i < result.getAttrCount(); i++) {
                    IDfAttr a = result.getAttr(i);
                    IDfValue v = result.getValueAt(i);
                    response += " "+a.getName() + '=' + v.asString();
                }
            }

            return response;
        }
        catch (DfException dfe){
            throw new RuntimeException(dfe);
        }
    }
    

}
