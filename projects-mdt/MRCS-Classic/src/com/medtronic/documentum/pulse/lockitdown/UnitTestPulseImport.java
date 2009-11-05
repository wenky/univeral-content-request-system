package com.medtronic.documentum.pulse.lockitdown;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.operations.IDfImportNode;
import com.documentum.operations.IDfImportOperation;

public class UnitTestPulseImport {
	
	public static void main(String[] cmdargs) throws Exception	
	{		
		
		Date dtStart = null;
        Format formatter = null;
        String dateStartFormat = null;

        dtStart = new Date();
        formatter = new SimpleDateFormat("MM.dd.yyyy_hh:mm:ss");
        dateStartFormat = formatter.format(dtStart);        

		System.out.println("test top");
		DfClientX dfx = new DfClientX();
       	IDfClient client = dfx.getLocalClient();
       	IDfSessionManager sMgr = client.newSessionManager();
       	IDfLoginInfo loginInfoObj = dfx.getLoginInfo();
        loginInfoObj.setUser("svc-mrcs1");
        loginInfoObj.setPassword("Crmsdo1");
        loginInfoObj.setDomain(null);
        sMgr.setIdentity("MRCS_Dev", loginInfoObj);
		IDfSession session = sMgr.getSession("MRCS_Dev");
        		
		// method arguments
		String DOCBASE = "MRCS_Dev";
		
		System.out.println("get id from cmdags");
		String newid = "09017f3f80075a64"; //09017f3f80075a60
		System.out.println("RETRIEVE "+newid);
//		IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newid));
		
		System.out.println("prep mthd call");
		
		IDfList args = new DfList(); 
		IDfList argsTypes = new DfList(); 
		IDfList argsValues = new DfList(); 		
		
		args.appendString("METHOD"); 
		argsTypes.appendString("S");
		argsValues.append("MrcsAutomatedImport");
		
		args.appendString("TRACE_LAUNCH"); 
		argsTypes.appendString("B");
		argsValues.appendString("T");		

		args.appendString("SAVE_RESULTS"); 
		argsTypes.appendString("B");
		argsValues.appendString("T");		
		
		args.appendString("ARGUMENTS"); //pass three command line arguments 		 
		argsTypes.appendString("S"); //command line args is a string 
//		-folder_id 0b017f3f80075368 
		String argumentValues = "-docbase_name " + DOCBASE + " -user svc-mrcs1 -ticket " 
			+ session.getLoginTicket() + " -folder_id 0b017f3f80075368 -document_id 09017f3f80075a64"
			+ " -ObjectName " + dateStartFormat;
		argsValues.appendString(argumentValues);

		//Execute the method
		// do we need to append user/sessionid to args? is that auto-included?		
		// do we need to be anal about closing the returned collection?
		System.out.println("invoke");
		IDfCollection returnval = session.apply(null, "DO_METHOD", args, argsTypes, argsValues);
		System.out.println("done, cleanup");
		
		while(returnval.next()) {
			System.out.println("Result::" + returnval.getString("result"));
			System.out.println("ResultDocId::" + returnval.getString("result_doc_id"));
			System.out.println("ProcessId::" + returnval.getString("process_id"));
			System.out.println("LaunchedFailed::" + returnval.getString("launch_failed"));
			System.out.println("MethodReturnVal::" + returnval.getString("method_return_val"));
			System.out.println("OSSystemError::" + returnval.getString("os_system_error"));
			System.out.println("TimedOut::" + returnval.getString("timed_out"));
			System.out.println("TimeOutLength::" + returnval.getString("time_out_length"));
		}
		
		try { returnval.close();}catch (Exception e) {}
		try { sMgr.release(session);}catch (Exception e) {}
		
	}

	
	
	public static void oldCarlDoesntWorkmain(String[] cmdargs) throws Exception	
	{		
		System.out.println("test top");
		DfClientX dfx = new DfClientX();
       	IDfClient client = dfx.getLocalClient();
       	IDfSessionManager sMgr = client.newSessionManager();
       	IDfLoginInfo loginInfoObj = dfx.getLoginInfo();
        loginInfoObj.setUser("svc-mrcs1");
        loginInfoObj.setPassword("Crmsdo1");
        loginInfoObj.setDomain(null);
        sMgr.setIdentity("MRCS_Dev", loginInfoObj);
		IDfSession session = sMgr.getSession("MRCS_Dev");
        
		// Code for client ops...
		//System.out.println("create import op");
		//IDfImportOperation impop = dfx.getImportOperation();
	    //IDfImportNode node = impop.add("<LocalFilePath>");
	    //node.setDocbaseObjectType("m_mrcs_document");
		//IDfList newobjs = impop.getNewObjects();
		//IDfSysObject newdoc = (IDfSysObject)newobjs.get(0);
		
		// method arguments
		String objDCTMDestinationFolderID = "";
		String DOCBASE = "MRCS_Dev";
		
		System.out.println("get id from cmdags");
		String newid = cmdargs[0];
		System.out.println("RETRIEVE "+newid);
		IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newid));
		
		System.out.println("prep mthd call");
		IDfList args = new DfList();
		IDfList types = new DfList();
		IDfList vals = new DfList();
		args.appendString("docbase_name");
		vals.appendString(DOCBASE);
		types.appendInt(IDfType.DF_STRING);
		//args.appendString("folder_id");
		//vals.appendString(objDCTMDestinationFolderID);
		//types.appendInt(IDfType.DF_STRING);
		args.appendString("document_id");
		vals.appendString(newdoc.getObjectId().getId());
		types.appendInt(IDfType.DF_STRING);
		
		// do we need to append user/sessionid to args? is that auto-included?
		
		// do we need to be anal about closing the returned collection?
		System.out.println("invoke");
		IDfCollection returnval = session.apply(objDCTMDestinationFolderID,"MrcsAutomatedImport",args,types,vals);
		System.out.println("done, cleanup");
		
		try { returnval.close();}catch (Exception e) {}
		try { sMgr.release(session);}catch (Exception e) {}
		
	}

}
