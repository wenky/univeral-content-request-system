import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;

public class TestESDMethodInvocation {
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
        loginInfoObj.setUser("muellc4");
        																														loginInfoObj.setPassword("redl1z@rd");
        loginInfoObj.setDomain(null);
        sMgr.setIdentity("mrcs", loginInfoObj);
		IDfSession session = sMgr.getSession("mrcs");
        		
		// method arguments
		String DOCBASE = "mrcs";
		
		System.out.println("get id from cmdags");
		String newid = "09025b418041cf92"; //09017f3f80075a60
		System.out.println("RETRIEVE "+newid);
//			IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newid));
		
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
//			-folder_id 0b017f3f80075368 
		String argumentValues = "-docbase_name " + DOCBASE + " -user hansom5! -ticket " 
			+ session.getLoginTicket() + " -folder_id 0b025b41803bdb71 -document_id 09025b41803e38eb"
			+ " -ObjectName TestMethod2";
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
		

}
