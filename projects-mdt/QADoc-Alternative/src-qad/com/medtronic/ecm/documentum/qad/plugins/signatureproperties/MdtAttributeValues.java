package com.medtronic.ecm.documentum.qad.plugins.signatureproperties;

import java.util.Iterator;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.qad.plugins.IMdtSignaturePropertiesPlugin;

public class MdtAttributeValues implements IMdtSignaturePropertiesPlugin 
{
    public void getProperties(IDfSysObject tboinstance, Map propertymap, String username, String justification, String signaturemethod,String appprops, String passthru1,String passthru2, Map pluginconfig) throws Exception
    {
        // get list of attributes to dump into propertymap
        /*-dbg-*/Lg.dbg("ITERATE on pluginconfigkeys");
        Iterator i = pluginconfig.keySet().iterator();
        while (i.hasNext())
        {
            /*-dbg-*/Lg.dbg("next attr");
            String key = (String)i.next();
            /*-dbg-*/Lg.dbg("...is %s",key);
            if (tboinstance.hasAttr(key)) {
                /*-dbg-*/Lg.dbg("...was found on tboinstance");
                if (tboinstance.isAttrRepeating(key))
                {
                    /*-dbg-*/Lg.dbg("...is repeating");
                    for (int j=0; j < tboinstance.getValueCount(key); j++)
                    {
                        String value = tboinstance.getRepeatingValue(key, j).asString();
                        String keyval = "mdt."+key+'_'+j;
                        /*-dbg-*/Lg.dbg("...--putting %s : %s",keyval,value);
                        propertymap.put(keyval, value);
                    }
                } else {
                    // single value
                    /*-dbg-*/Lg.dbg("...singleval");
                    String value = tboinstance.getValue(key).asString();
                    /*-dbg-*/Lg.dbg("...--putting %s : %s",key,value);                    	
                    propertymap.put("mdt."+key,value);
                }
            }
        }
        /*-dbg-*/Lg.dbg("DONE");
    }
    
    public static void main(String[] args)
    {
        try { 
            MdtAttributeValues mdtmsg = new MdtAttributeValues();
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_dev", loginInfoObj);             
            IDfSession sess = sMgr.getSession("mqadoc_dev");
            
            IDfSysObject doc = (IDfSysObject)sess.getObject(new DfId("09017f448002d993"));
            doc.addESignature("ecsadmin", "password", "justification", "pdf", "", "", "", "", "", "");
            
            sMgr.release(sess);
        } catch (Exception ez) {
            int i = 1;
            i++;
            
        }        
        
    }

}
