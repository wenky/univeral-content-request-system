package com.medtronic.ecm.documentum.common.unittest;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;

public class TestEnrichableException 
{
	public static void main (String[] args) {
		TestEnrichableException tee = new TestEnrichableException();
		try { 
			tee.level1();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int i=0;
		i++;
	}
	
    public void level1(){
        try{
            level2();
        } catch (Exception e){
            throw EEx.create("E1", "Error in level 1, calling level %s","2",e);
        }
    }

    public void level2(){
        try{
            level3();
        } catch (Exception e){
        	IDfSysObject s = null;
        	try { 
	        IDfClientX clientx = new DfClientX();
	       	IDfClient client = clientx.getLocalClient();
	       	IDfSessionManager sMgr = client.newSessionManager();
	       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
	        loginInfoObj.setUser("ecsadmin");
	        loginInfoObj.setPassword("spring2005");
	        loginInfoObj.setDomain(null);
	        sMgr.setIdentity("sandbox6", loginInfoObj);        	    
	        IDfSession sess = sMgr.getSession("sandbox6");
	        s = (IDfSysObject)sess.getObject(new DfId("0b1e666380003f6f"));
            sMgr.release(sess);
        	} catch (Exception ez) {}
            throw EEx.create("E2", "Error in level 2, calling level 3, SYSOBJ -- %s",s,3,e);
        }
    }

    public void level3(){
        try{
            level4();
        } catch(IllegalArgumentException e){
        	throw EEx.create("E3", "Error at level %d",3,e);
        }
    }

    public void level4(){
        throw new IllegalArgumentException("incorrect argument passed");
    }

}
