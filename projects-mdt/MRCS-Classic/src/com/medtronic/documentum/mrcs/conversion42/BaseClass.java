package com.medtronic.documentum.mrcs.conversion42;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import com.thoughtworks.xstream.XStream;

public class BaseClass 
{
	// need to set these before execution....
	
	// -- mrcs prod clone --
	//static String user = "mradmin";
	//static String pass = "mr2006";
	//static String docbase = "mrcs";
	//static String conversiondirectory = "C:/2006WT53/conversion42files/";

	// mrcs training
	//static String user = "mradmin";
	//static String pass = "COntent";
	static String user = "svc-mrcs1";
	static String pass = "Crmsdo1";
	static String docbase = "mrcs";
	//static String conversiondirectory = "/user/mradmin/MRCS421CONVERSION/";
	static String conversiondirectory = "C:/2006WT53/conversion42files/";

	// provide XStream services and docbase login...
	public static IDfSessionManager sMgr = null;
	
	public static XStream xs;
	static {
		xs = new XStream();
        xs.alias("ConversionOperation",ConversionOperation.class);
        xs.alias("DocbaseObjectRecord",DocbaseObjectRecord.class);
	}
	
	public static IDfSession getSession()
	{
		try {
			if (sMgr == null) {
		        IDfClientX clientx = new DfClientX();
		       	IDfClient client = clientx.getLocalClient();
		       	sMgr = client.newSessionManager();
		       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		        loginInfoObj.setUser(user);
		        loginInfoObj.setPassword(pass);
		        loginInfoObj.setDomain(null);
		        sMgr.setIdentity(docbase, loginInfoObj);
			}
	        return sMgr.getSession(docbase);
        }
        catch (DfException dfe){
        	throw new RuntimeException(dfe);
        }
	}
		
	public static void writeDocumentList(String filename, List docs)
	{

		File f = new File(filename);

        BufferedWriter filewriter = null;
        try {
        	FileOutputStream fis = new FileOutputStream(f);
        	OutputStreamWriter isr = new OutputStreamWriter(fis,"UTF8");
            filewriter = new BufferedWriter(isr);
        } catch (IOException ioe) {
        	throw new RuntimeException(ioe);
        }
        xs.toXML(docs,filewriter);
        try {
        	filewriter.close();
        } catch (IOException ioe) {
        	throw new RuntimeException(ioe);
        }
		
	}
	
	public static List readDocumentList(String filename)
	{

		File f = new File(filename);

        BufferedReader filereader = null;
        try {
        	FileInputStream fis = new FileInputStream(f);
        	InputStreamReader isr = new InputStreamReader(fis,"UTF8");
            filereader = new BufferedReader(isr);
        } catch (IOException ioe) {
        	throw new RuntimeException(ioe);
        }
        
        List items = (List)xs.fromXML(filereader);
		
        try {
        	filereader.close();
        } catch (IOException ioe) {
        	throw new RuntimeException(ioe);
        }
		
        return items;
	}
	

}
