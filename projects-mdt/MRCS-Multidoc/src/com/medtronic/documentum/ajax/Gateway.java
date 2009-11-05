/*
 * Created on Oct 13, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.ajax;


import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.DfPrincipalException;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.operations.IDfImportOperation;
import com.documentum.operations.contentpackage.IDfClientServerFile;
import com.documentum.operations.contentpackage.IDfContentPackageFactory;
import com.documentum.operations.contentpackage.IDfImportPackage;
import com.documentum.operations.contentpackage.IDfImportPackageItem;
import com.documentum.ucf.common.UCFException;
import com.documentum.ucf.server.contentpackage.IPackageProcessor;
import com.documentum.ucf.server.contentpackage.IPackageProcessorFactory;
import com.documentum.ucf.server.transport.IServerSession;
import com.documentum.web.common.ClientInfo;
import com.documentum.web.common.ClientInfoService;
import com.documentum.web.contentxfer.ContentTransferConfig;
import com.documentum.web.contentxfer.ucf.UcfTransportManager;
import com.documentum.web.formext.config.ConfigService;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.util.Base64;
import com.documentum.web.util.DfcUtils;
import com.documentum.web.util.SafeHTMLString;
import com.documentum.web.util.StringUtil;

/**
 * @author muellc4
 *
 * DWR calls come from here...
 */

public class Gateway
{
	static IDfClientX clientx = new DfClientX();
	public IDfSessionManager sMgr = null;
	
	public List getAvailableDocbases() throws Exception
	{		
       	IDfDocbaseMap map = clientx.getDocbrokerClient().getDocbaseMap();
       	List doclist = new ArrayList();
       	for (int i =0; i < map.getDocbaseCount(); i++)
       	{
       		doclist.add(map.getDocbaseName(i));
       	}
       	return doclist;
	}
	
	public boolean authenticate(String user, String pass, String base)
	{
		IDfSession session = getSession(user,pass,base);
		if (session == null) 
			return false;
		
		sMgr.release(session);
		return true;
	}
	
	// I assume this class is instantiated for each DWR request/response cycle...
	public IDfSession getSession(String user, String pass, String base)
	{
		try {
			if (sMgr == null) {
		       	IDfClient client = clientx.getLocalClient();
		       	sMgr = client.newSessionManager();
		       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		        loginInfoObj.setUser(user);
		        loginInfoObj.setPassword(pass);
		        loginInfoObj.setDomain(null);
		        sMgr.setIdentity(base, loginInfoObj);
			}
	        return sMgr.getSession(base);
        } catch (DfIdentityException ie){
        	return null;
        } catch (DfAuthenticationException ae){
        	return null;
        } catch (DfPrincipalException pe){
        	return null;
        } catch (DfException dfe){
        	throw new RuntimeException(dfe);
        }
	}

	public List getCabinetList (String user, String pass, String base) throws Exception
	{
		int i=0;
		IDfSession session = getSession(user,pass,base);
		try {
			// need a query utility here...
			return getQueryResult(session,"select upper(object_name),r_object_id,object_name,r_object_type,owner_name,r_link_cnt,i_is_replica,i_is_reference  from dm_cabinet where (is_private=0) and a_is_hidden=false Union select upper(object_name),r_object_id,object_name,r_object_type,owner_name,r_link_cnt,i_is_replica,i_is_reference  from dm_cabinet where (owner_name=USER) and a_is_hidden=false order by 1");			
		} finally { sMgr.release(session); }		
	}

	
	public List getFolderList(String user, String pass, String base, String folderid) throws Exception
	{
		IDfSession session = getSession(user,pass,base);
		try {
			// need a query utility here...
			// SELECT r_object_id, object_name, r_link_cnt FROM dm_folder WHERE any i_folder_id = 'parentfolderid' ORDER BY object_name
			return getQueryResult(session,"SELECT upper(object_name),r_object_id,object_name,r_object_type,owner_name,r_link_cnt FROM dm_folder WHERE any i_folder_id = '"+folderid+"' ORDER BY 1");			
		} finally { sMgr.release(session); }		
	}
	
	public Map getDirectoryContents(String user, String pass, String base, String folderid) throws Exception
	{
		IDfSession session = getSession(user,pass,base);
		try {
			// need a query utility here...
			// SELECT r_object_id, object_name, r_link_cnt FROM dm_folder WHERE any i_folder_id = 'parentfolderid' ORDER BY object_name
			HashMap results = new HashMap();
			
			class columndescription {
				public String column,label; public Integer order; public Boolean selected;
				columndescription(String column,String label, int order, boolean selected) { this.column = column; this.label = label; this.order = new Integer(order); this.selected = new Boolean(selected); }
			}
			
			List foldercolumns = new ArrayList();
			foldercolumns.add(new columndescription("r_object_id","Object ID",0,false));
			foldercolumns.add(new columndescription("object_name","Name",1,true));
			foldercolumns.add(new columndescription("r_object_type","Type",2,false));
			foldercolumns.add(new columndescription("r_lock_owner","Locked By",3,false));
			foldercolumns.add(new columndescription("owner_name","Owner",4,false));
			foldercolumns.add(new columndescription("r_link_cnt","Subfolder Count",5,false));
			foldercolumns.add(new columndescription("r_is_virtual_doc","Virtual Document",6,false));
			foldercolumns.add(new columndescription("r_content_size","Size",7,false));
			foldercolumns.add(new columndescription("a_content_type","Format",8,false));
			foldercolumns.add(new columndescription("title","Title",9,false));
			foldercolumns.add(new columndescription("r_modify_date","Last Modified",10,true));
			results.put("foldercolumns",foldercolumns);			
			List folderlist = getQueryResult(session,"SELECT 1,upper(object_name) as sortname,r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
		                                                    "r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,"+
		                                                    "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,'1' as isfolder "+
		                                             "FROM dm_folder WHERE a_is_hidden=false and any i_folder_id='"+folderid+"' order by 1,2");
			results.put("folders",folderlist);
			List filecolumns = new ArrayList();
			filecolumns.add(new columndescription("r_object_id","Object ID",0,false));
			filecolumns.add(new columndescription("object_name","Name",1,true));
			filecolumns.add(new columndescription("r_object_type","Type",2,false));
			filecolumns.add(new columndescription("r_lock_owner","Locked By",3,true));
			filecolumns.add(new columndescription("owner_name","Owner",4,false));
			filecolumns.add(new columndescription("r_link_cnt","Subfolder Count",5,false));
			filecolumns.add(new columndescription("r_is_virtual_doc","Virtual Document",6,false));
			filecolumns.add(new columndescription("r_content_size","Size",7,true));
			filecolumns.add(new columndescription("a_content_type","Format",8,true));
			filecolumns.add(new columndescription("title","Title",9,true));
			filecolumns.add(new columndescription("r_modify_date","Last Modified",10,true));
			results.put("filecolumns",filecolumns);			
		    List filelist = getQueryResult(session,"SELECT 2,upper(object_name) as sortname,r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,"+
		    											  "r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,r_has_frzn_assembly,"+
		    											  "a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,'0' as isfolder " +
		                                           "FROM dm_document where a_is_hidden=false and any i_folder_id='"+folderid+"' order by 1,2");

		    results.put("files",filelist);
		    return results;
			
		} finally { sMgr.release(session); }
	}
	
	public List getQueryResult(IDfSession session, String query) throws Exception
	{
        IDfQuery qry = new DfQuery();
        qry.setDQL(query);
        IDfCollection myObj1 = null; 
        try {
	        List columns = new ArrayList();
	    	List results = new ArrayList();
        	try { 
        		myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
        	} catch (NullPointerException npe) {
        		// empty result set == NullPointerException, another sterling design decision by DCTM
        		return results;
        	}
            // get metadata
	    	for (int i=0; i < myObj1.getAttrCount(); i++)
	    	{
	    		columns.add(myObj1.getAttr(i).getName());
	    	}
	    	// get result rows
	        while (myObj1.next()) {
	        	Map row = new HashMap();
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
	        		if (myObj1.getAttr(i).isRepeating()) {
	        			List repeatingvalues = new ArrayList();
	        			for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
	        			{
	        				myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
			        		switch (myObj1.getAttr(i).getDataType()) {
			        			case IDfAttr.DM_BOOLEAN : repeatingvalues.add(new Boolean(myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_DOUBLE  : repeatingvalues.add(new Double(myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_ID      : repeatingvalues.add(myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId()); break;  
			        			case IDfAttr.DM_INTEGER : repeatingvalues.add(new Integer(myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_TIME    : repeatingvalues.add(myObj1.getRepeatingTime(myObj1.getAttr(i).getName(),j).getDate()); break;
			        			case IDfAttr.DM_STRING  : repeatingvalues.add(myObj1.getRepeatingString(myObj1.getAttr(i).getName(),j)); break;
			        		}
	        			}
	        			row.put(columns.get(i),repeatingvalues);
	        		} else {
		        		switch (myObj1.getAttr(i).getDataType()) {
		        			case IDfAttr.DM_BOOLEAN : row.put(columns.get(i),new Boolean(myObj1.getBoolean(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_DOUBLE  : row.put(columns.get(i),new Double(myObj1.getDouble(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_ID      : row.put(columns.get(i),myObj1.getId(myObj1.getAttr(i).getName()).getId()); break;  
		        			case IDfAttr.DM_INTEGER : row.put(columns.get(i),new Integer(myObj1.getInt(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_TIME    : row.put(columns.get(i),myObj1.getTime(myObj1.getAttr(i).getName()).getDate()); break;
		        			case IDfAttr.DM_STRING  : row.put(columns.get(i),myObj1.getString(myObj1.getAttr(i).getName())); break;
		        		}
	        		}
	        	}
	        	results.add(row);
	        }
	        myObj1.close();
	    	
	    	return results;
        } finally {if (myObj1 != null) myObj1.close();}
	}
	
	
	public Map call(String user, String pass, String base, String pluginname, Map parameters) throws Exception
	{
		GatewayPlugin plugin = (GatewayPlugin)Class.forName(pluginname).newInstance();
		IDfSession session = getSession(user,pass,base);
		try { 
			return plugin.execute(session,parameters);
		} finally {
			sMgr.release(session);
		}
	}
	
	public Map getUCFAppletData()
	{
		// get HttpServletRequest of AJAX request from threadlocal
		WebContext ctx = WebContextFactory.get();
		HttpServletRequest request = ctx.getHttpServletRequest();
		
		Map appletparams = new HashMap();
		
		// jsessionid is probably the most important, it is a session id for the UCF
		// -- technically, I like to avoid sessions given the philosophy of pushing the session
		//    to the client, but due to UCF architecture, a session seems necessary. We will 
		//    make a timeout-durable scheme however. I _think_ we can do that...
        String sessionId = null; // this creates a session if necessary
        if (sessionId == null)
        	// create new session. Do we need to explicitly set the JSESSIONID cookie?
        	sessionId = request.getSession().getId();
        // find the session cookie value...
        Cookie cookies[] = request.getCookies(); // look for a cookie override of this session id...
        if(cookies != null && cookies.length > 0)
        {
            for (int i = 0; i < cookies.length; i++) 
            {
                if(cookies[i].getName() != null && cookies[i].getName().equals("JSESSIONID"))
                {
                    sessionId = cookies[i].getValue();
                    break;
                }
            }
        }
        appletparams.put("JSESSN_ID",sessionId);
        
        IConfigElement ucfElem = ConfigService.getConfigLookup().lookupElement("application.contentxfer.ucf", Context.getSessionContext());
        if(ucfElem != null)
        {
            StringBuffer headers = new StringBuffer();
            Iterator iter = ucfElem.getChildElements("request-header");
            do
            {
                if(!iter.hasNext())
                    break;
                IConfigElement h = (IConfigElement)iter.next();
                String hname = h.getAttributeValue("name");
                if(hname != null && hname.length() > 0)
                {
                    String hvalue = h.getAttributeValue("value");
                    if(hvalue == null || hvalue.length() == 0)
                        hvalue = request.getHeader(hname);
                    if(hvalue != null)
                    {
                        if(headers.length() > 0)
                            headers.append(';');
                        headers.append(hname).append("=").append(Base64.encode(hvalue));
                    }
                }
            } while(true);
            if(headers.length() > 0)
                appletparams.put("PARAM_UCF_LAUNCH_HEADERS", headers.toString());
        }
        
        
        // this is hardcoded...
        appletparams.put("PARAM_UCF_LAUNCH_EXCLUDE_COOKIES", "wdk_pref_cookie_0;wdk_sess_cookie_0;appname;lockFresh;allFreshClient;__dmfClientId");
        
        ClientInfo clientInfo = ClientInfoService.getInfo();
        if(clientInfo.isBrowser(ClientInfo.MSIE) && clientInfo.isPlatform(ClientInfo.WIN))
        {
        	appletparams.put("cabbase", "ucfinit.cab");
        } 
        appletparams.put("cache_option", "Plugin");
        appletparams.put("cache_archive", "ucfinit.jar");
        appletparams.put("UCF_ID_PARAM", "__dmfUcfClientId");
        appletparams.put("CONTX_PATH", request.getContextPath());
        
        // PostServer "support"
        //renderAppletParameter("HNDLR_URL", getFormActionURL(), out);  // speculation: this is the server request call in three parts. part one: servlet 
        //renderAppletParameter("HNDLR_ID", form.getElementName(), out);//    part two: parameter one, a "handler_id" <-- these seem wdk specific, but do they need to be?
        //renderAppletParameter("RQST_ID", form.getFormRequest().getRequestId().toString(), out); // part three: parameter two, a "request_id" <-- perhaps we set these values in the extant session now?
        //appletparams.put("HNDLR_URL","/webtop2/zzzUcfCallbackTest.jsp");
        appletparams.put("HNDLR_URL","/webtop2/testucf");
        appletparams.put("HNDLR_ID","324890238049");
        appletparams.put("RQST_ID","reqid");
        
        // --PERHAPS I should IFRAME the ucf applet, in case it auto-redirects the client (which we don't want to do in our AJAX environment)
        // --may be hard to script it then though...
        // -- OR PERHAPS: if we leave this stuff out, it won't do any redirects at all? But we need UCF upload status somehow...hmm....
        //
        
        // this is hardcoded for all applets apparently?
        // -- use a different servlet...??? Need to look at wdk-appletresultsink..
        appletparams.put("RSLT_SRVLT", SafeHTMLString.escapeAttribute(request.getContextPath() + "/wdk5-appletresultsink"));  
        
        String reqKey = getUniqueKey();
        appletparams.put("UCF_REQ_KEY_PARAM", "__dmfUcfClientReqKey");
        appletparams.put("UCF_REQ_KEY_VALUE", reqKey);
        appletparams.put("USER_LOC_PATH", ContentTransferConfig.getConfig().getClientUserLocation());
        
        appletparams.put("ON_CONNECT_HNDLR_NM", "ConnectHandler"); // connect "handler"
        appletparams.put("ON_UNABLE_LOCATE_JAVA_HNDLR_NM", "NoJavaHandler");
        appletparams.put("ON_DELETE_ON_INSTALL_FAILED_HNDLR_NM", "InstallFailedHandler");
        if(clientInfo.isPlatform(ClientInfo.MACOS) && clientInfo.isBrowser(ClientInfo.SAFARI))
        	appletparams.put("appletName515f7ddee55e428b964a3839a78fbaa6", "OSXSafariControl");
        UcfTransportManager manager = UcfTransportManager.getManager();
        appletparams.put("PARAM_UCF_LAUNCHER_MODE", String.valueOf(manager.getLauncherMode()));
        appletparams.put("PARAM_UCF_COOKIE_VALUE", String.valueOf(manager.getLauncherCookie()));
        
        return appletparams;
	}
	
    private String getUniqueKey()
    {
        String uid = (new VMID()).toString();
        int len = uid.length();
        StringBuffer buf = new StringBuffer(len + 10);
        for(int i = 0; i < len; i++)
        {
            char c = uid.charAt(i);
            if(c < '\200' && Character.isLetterOrDigit(c))
                buf.append(c);
            else
                buf.append(StringUtil.toUnsignedString(c, 5));
        }

        return buf.toString();
    }
	
    public Map importSingleFile(Map params) throws DfException,UCFException
    {
    	// In general:
    	// then preprocess(pkg) <-- one call per processor/file
    	// then preprocess(transport) <-- one call (I think this informs DFC about UCF)
    	// then preprocess(op) <-- one call per processor/file
    	// then execute op
    	// then postprocess(pkg) <-- one call per processor/file
    	// then postprocess(transport) <-- one call (I think this informs DFC about UCF)
    	// then postprocess(op) <-- one call per processor/file
    	
    	// for Import specifically, there appears to only be preprocess(pkg) code (see the ImportProcessor class), and the transport pre/post processing necessary
    	// Also, this call is designed for a single file import only, so we don't loop on the packages. 
    	
    	String localfile = (String)params.get("localfile");
    	String localmachine = (String)params.get("localmachine");
    	String objectname = (String)params.get("objectname");
    	String objecttype = (String)params.get("objecttype");
    	String objectformat = (String)params.get("objectformat");
    	String metadata = (String)params.get("metadata");
    	String targetfolderid = (String)params.get("targetfolderid");
    	String user = (String)params.get("user"); String pass = (String)params.get("pass"); String base = (String)params.get("base");
    	
    	Map returnvals = new HashMap();
    	
    	IDfSession session = null;
    	try { 
	    	session = getSession(user,pass,base);
	    	
	    	NewUcfSessionManager ucfSessionMgr = NewUcfTransportManager.getManager().getSessionManager();
			IServerSession ucfsession = ucfSessionMgr.getSession();		
	    	
			// do DFC object creation (the deconstruction seems to indicate basic, simple object instantiations, nothing too fancy...)
			IDfImportOperation dfOp = DfcUtils.getClientX().getImportOperation(); // IDfOperation is the generic class
			IDfImportPackage pkg = DfcUtils.getClientX().getContentPackageFactory().newImportPackage(); // IDfPackage is the generic class
			pkg.setDestinationFolderId(new DfId(targetfolderid));
			// from the Inbound service that underlies the Import service
			ClientInfo clientInfo = ClientInfoService.getInfo();			
	        if(clientInfo.isPlatform(ClientInfo.MACOS))
	            pkg.setMacResourceForkOption(2);
	        else
	            pkg.setMacResourceForkOption(1);
			// PRETTY sure that in multiple-file import processes, there is still one "package" that wraps all the files to import. we're going to do it one file at a time for now...
	        
	        // UcfContentTransport prep code: I think this associates/tells DFC to use UCF (from UcfContentTransport.createPackageProcessor)
            IPackageProcessorFactory pf = ucfsession.getPackageProcessorFactory();
            IPackageProcessor pproc = pf.newImportPackageProcessor(dfOp,pkg);
            //ucfsession.setNotificationMonitor(getProgressMonitor()); // <-- need a progress monitor facility....
            pproc.preProcess(); 
	        
	        // From ImportProcessor.preProcess 
            IDfClientServerFile csfile = DfcUtils.getClientX().getContentPackageFactory().newClientServerFile();
            IDfContentPackageFactory pfactory = DfcUtils.getClientX().getContentPackageFactory();
            
            // ?? SOmething to do with client and server files, and its either one or the other. Need to runthru the MrcsImportContentUCF's ImportProcessor prep code...
            String filePath = ""; // For me, this will be some combo of localfile + localmachine???
            if(filePath != null && filePath.length() > 0)
            {
                csfile.setClientFile(pfactory.newClientFile(filePath));
            } else
            {
                filePath = ""; // <-- no f'ing clue so far what this does. Maybe this is for files already on the Webtop server somewhere and don't need UCF transfer
                csfile.setServerFile(pfactory.newServerFile(filePath));
            }
            csfile.setIsDirectory(false); // <-- I don't do whole-directory imports. I'll write a magical wizard to do that...
            IDfImportPackageItem item = null;
            // WTF is this?
            String clientparentpath = ""; // What exactly is the Client Parent Path?
            if(clientparentpath == null || clientparentpath.length() == 0)
            {
                item = pkg.add(csfile);
            } else
            {
            	// I think this is for imported directories...???...perhaps for virtual docs...
                //IDfImportPackageItem parent = findParentItem(pkg);
                //if(parent != null)
                //    item = pkg.add(parent, csfile);
                //else
                //    item = pkg.add(csfile);
            	int a=1;
            	a++;
            }
            item.setObjectName(objectname);
            item.setObjectType(objecttype);
            item.setFormat(objectformat);
            // setItemAttributes(item, getContext().getDfSession().getType(objecttype), metadata); // <-- InboundProcessor has tons of code for this. We'll need to preprep the metadata though...
	        // End ImportProcessor.preProcess code 

            // add package to operation - from ContentTransferService.execute()
            dfOp.add(pkg);
            boolean suceeded = dfOp.execute(); // check ContentTransferService.handleOperationError for operation error processing code. we'll ignore errors for now of course :-)
            
            // PostProcess? <-- I don't think Import has any...
            pproc.postProcess();  // this is the transport postprocess

            ucfSessionMgr.release(ucfsession);
	        
    	} catch (UCFException ucfe) {
    		int b = 1;
    		b = 2;
    		throw ucfe;
    	} catch (DfException e) {
    		int b = 1;
    		b = 2;
    		throw e;
    	} finally { if (session != null) sMgr.release(session); }
    	
    	return returnvals;
    	
    }

}
