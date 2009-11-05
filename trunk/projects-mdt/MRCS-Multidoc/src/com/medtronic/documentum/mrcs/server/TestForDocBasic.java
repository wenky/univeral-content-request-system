package com.medtronic.documentum.mrcs.server;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

public class TestForDocBasic 
{
	public String getAString() {return "hello from javaworld"; }
	
	public void letsdoit(String sessionid, String docid)
	{
		/*-CONFIG-*/String m="letsdoit-";
		try {
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->top - create ClientX", null,null);
			IDfClientX clientx = new DfClientX();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->get client from ClientX", null,null);
			IDfClient client = clientx.getLocalClient();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->adopt session "+sessionid, null,null);
			IDfSession session = client.adoptDMCLSession(sessionid);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->session adopted", null,null);
			
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->looking up document "+docid, null,null);
			IDfDocument doc = (IDfDocument)session.getObject(new DfId(docid));
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->setting subject attr", null,null);
			doc.setString("subject","letsdoit was here");
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->subject attr set", null,null);
			
		} catch (Exception e) {
	        /*-ERROR-*/DfLogger.error(this,m+": -->subject attr set", null,null);
			throw new RuntimeException("MRCS Error",e);
		}
		
		
	}

}
