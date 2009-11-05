package test;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.GroovyExecute;

public class TestGroovyFromJSP {

    public static String doit() 
    {
        try {
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_test", loginInfoObj);             

            IDfSession sess = null;
            try {
                sess=sMgr.getSession("mqadoc_test");

                String mystr = GroovyExecute.groovyTemplate(sess,"/SMO/Admin-MDT/GroovyTest",GroovyExecute.createBinding("hi","there"));
                return mystr;
            } finally{
                sMgr.release(sess);
            }
        
        } catch (Exception e) {
           String outerr = e.getMessage();
           return outerr;
        }
        
    }
}
