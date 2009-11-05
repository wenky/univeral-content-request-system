package com.medtronic.ecm.documentum.core.webtop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.env.PreferenceRepository;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.session.Login;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.tsgrp.util.TsgEncrypter;

public class MdtLoginWithAppSelection extends Login
{

    public static final String CONTROL_APPLICATION = "MdtApplication";
    public static final String DOCBASE_LABEL = "MdtDocbase";

    String   mdtdocbase_ = "";

    public void onInit(ArgumentList args)
    {
        /*-INFO-*/Lg.inf("Init - calling super()");
    	super.onInit(args);

    	// get docbase from component config
        /*-dbg-*/Lg.dbg("look up webtop's assigned MdtDocbase from component config");
    	mdtdocbase_ = this.lookupString("MdtDocbase");
        /*-dbg-*/Lg.dbg(" -- docbase: %s",mdtdocbase_);

        /*-dbg-*/Lg.dbg("init app dropdown with list");
    	initApplicationDropDown();
        /*-dbg-*/Lg.dbg("done with init");
    }



    // we will be hiding the actual dropdown and providing only a label for the single docbase that the webtop instance is allowed to connect to.
    // this makes sure the value is set down the class inheritence stack. If DCTM wouldn't make everything private, I wouldn't have to hack
    // this crap.
    protected void initDocbaseDropdown(String strDocbase)
    {
        /*-INFO-*/Lg.inf("Top - calling super()");
    	super.initDocbaseDropdown(strDocbase);
        /*-dbg-*/Lg.dbg("get hidden docbase dropdown");
        DataDropDownList listDocbases = (DataDropDownList)getControl("docbase", DataDropDownList.class);
        try {
            /*-dbg-*/Lg.dbg("set value to docbase %s",mdtdocbase_);
	        listDocbases.setValue(mdtdocbase_);
            /*-dbg-*/Lg.dbg("trigger event");
	        onSelectDocbaseFromDropDown(listDocbases,null);
            /*-dbg-*/Lg.dbg("get docbase label control");
	        Label docbaselabel = (Label)getControl(DOCBASE_LABEL, Label.class);
            /*-dbg-*/Lg.dbg("setlabel");
	        docbaselabel.setLabel(mdtdocbase_);
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-WARN-*/Lg.wrn("Error defaulting docbase dropdown, docbroker is probably down...",e);
			setErrorMessage("MDT_DOCBASE_DEFAULT_ERROR_CHECK_DOCBROKER");
        }
    }


    String loginticket = null;
	public void onTsgLogin()
	{

	   try
	   {
		   //TODO encrypt the login ticket
		   loginticket = new TsgEncrypter().encrypt(loginticket);
	   }
	   catch( Exception e)
	   {
		   /*-WARN-*/Lg.wrn("could not encrypt password for TSG AW Connector on login",e);
		   throw EEx.create("Login-TSGpwd","could not encrypt password for TSG AW Connector on login",e);
	   }

	   //put the login ticket into session
	   HttpSession httpSession = getPageContext().getSession();
	   httpSession.setAttribute("loginticket", loginticket);
	}

	
	
	// no DfSession until after super.onLogin...
    public void onLogin(Button loginButton, ArgumentList arg)
    {
        /*-INFO-*/Lg.inf("Top");
        /*-dbg-*/Lg.dbg("tuck pwd away before super clears it...");
        loginticket = ((Password)getControl("password")).getValue();        
        /*-dbg-*/Lg.dbg("calling super()");
    	super.onLogin(loginButton, arg);
    	// do we have a session now?
    	try {
            /*-dbg-*/Lg.dbg("get session (we should have one now...)");
    		IDfSession session = getDfSession();
        	// store Active Wizard connector credential
            /*-dbg-*/Lg.dbg("calling onTsgLogin");
        	onTsgLogin();
    		
            /*-dbg-*/Lg.dbg("get current user");
    		String username = session.getLoginUserName();
            /*-INFO-*/Lg.inf("Username: %s",username);
            /*-dbg-*/Lg.dbg("get MdtApplication select dropdown control");
    		DataDropDownList appdd = (DataDropDownList)getControl(this.CONTROL_APPLICATION,DataDropDownList.class);
            /*-dbg-*/Lg.dbg("get application value");
    		String application = appdd.getValue();
            /*-INFO-*/Lg.inf("MdtApp: %s",application);
            IDfGroup allowgroup = getDfSession().getGroup("mdtallow_"+application);
            if (!allowgroup.isUserInGroup(username)) {
                throw new DfServiceException("User not configured to use application "+application);
            }            
            MdtConfigService cfg = MdtConfigService.getConfigService(session.getSessionManager(), session.getDocbaseName());
            cfg.setUniqueMdtApplicationForUser(session.getLoginUserName(), application);
            /*-dbg-*/Lg.dbg("done");
    	} catch (DfServiceException dfse) {
    		/*-WARN-*/Lg.wrn("user is not a member of the application's group of valid users",dfse);
    		if ("DM_GROUP_E_NOT_DYNAMIC_MEMBER".equals(dfse.getMessageId())){
    			setErrorMessage("DM_GROUP_E_NOT_DYNAMIC_MEMBER");
    		}
    	} catch(DfException err) {
    		/*-WARN-*/Lg.wrn("generic DfException error in custom onLogin handler",err);
            String strOutput = getLocalizedError(err.getMessage());
            if(strOutput == null)
                strOutput = err.getMessage();
            setErrorMessage(strOutput);
        } catch (NullPointerException npe) {
        	// occurs on invalid loginname
			//setErrorMessage("DM_INVALID_USERNAME");
        	/*-do nothing */return;
        }
    }

    
    
    public void initApplicationDropDown()
    {
        /*-INFO-*/Lg.inf("Init MdtApp dropdown");
        /*-dbg-*/Lg.dbg("lookup control");
        DataDropDownList listApps = (DataDropDownList)getControl(CONTROL_APPLICATION, DataDropDownList.class);
        /*-dbg-*/Lg.dbg("get applist result set");
        ScrollableResultSet applist = getApplicationListResultSet();
        /*-dbg-*/Lg.dbg("bind applist result set/list to dropdown widget");
        listApps.getDataProvider().setScrollableResultSet(applist);
        /*-dbg-*/Lg.dbg("done");
    }


    protected ScrollableResultSet getApplicationListResultSet()
    {
        /*-INFO-*/Lg.inf("look up webtop's application code in config");
        String appcodefromconfig = this.lookupString("MdtContext");
        if (appcodefromconfig != null) {
            String appcodefromconfigname = this.lookupString("MdtContextName");
            /*-dbg-*/Lg.dbg("return applist code: %s label: %s",appcodefromconfig,appcodefromconfigname);
            TableResultSet applicationlist = new TableResultSet(new String[] {"application_name", "label_text"});;
            applicationlist.add(new String[] {appcodefromconfig,appcodefromconfigname});
            return applicationlist;
        }
        
        /*-INFO-*/Lg.inf("look up webtop's applicatoin list from repository");
        /*-dbg-*/Lg.dbg("get application context from component config");
    	String MdtApplicationContext = this.lookupString("MdtApplistFile");
        /*-dbg-*/Lg.dbg("  app context: %s",MdtApplicationContext);
    	try {
	        /*-dbg-*/Lg.dbg("get preference repository instance");
	        PreferenceRepository prefrep = PreferenceRepository.getInstance();
	        /*-dbg-*/Lg.dbg("get SESSION from pref repository session manager");
	        IDfSession prefsess = prefrep.getRepositorySession();
	        //password = TrustedAuthenticatorUtils.decryptByDES(password);
	        /*-dbg-*/Lg.dbg("  SESSION docbase: %s",prefsess.getDocbaseName());
	        /*-dbg-*/Lg.dbg("  SESSION user: %s",prefsess.getLoginUserName());
	        /*-dbg-*/Lg.dbg("get applist configuration sysobject");
	        String qual = "dm_sysobject where object_name = '"+MdtApplicationContext+".applist'";
	        /*-dbg-*/Lg.dbg("  qual: %s",qual);
	    	IDfSysObject applistdocument = (IDfSysObject)prefsess.getObjectByQualification(qual);
	        /*-dbg-*/Lg.dbg("  sysobject: %s",applistdocument);
	        /*-dbg-*/Lg.dbg("take keywords as applist - iterate...");
	    	TableResultSet applicationlist = new TableResultSet(new String[] {"application_name", "label_text"});;
	    	for (int i=0; i < applistdocument.getValueCount("keywords"); i++)
	    	{
		        /*-dbg-*/Lg.dbg("  -iteration #%d",i);
	    		String appname = applistdocument.getRepeatingString("keywords", i);
	    		String applabel = applistdocument.getRepeatingString("authors", i);
		        /*-dbg-*/Lg.dbg("  -add appname %s label %s",appname,applabel);
		        applicationlist.add(new String[] {appname,applabel} );
	    	}
	        /*-dbg-*/Lg.dbg("returning application list");
	    	return applicationlist;

    	} catch (DfServiceException dfse) {
            /*-WARN-*/Lg.wrn("Error defaulting MDT application list dropdown, Global Registry is probably not setup correctly",dfse);
			throw EEx.create("MDT_APPLIST_SERVICE_ERROR","Global Registry likely down or not configured",dfse);
    	} catch (DfException dfe) {
            /*-WARN-*/Lg.wrn("Error defaulting MDT application list dropdown, document probably not found",dfe);
			throw EEx.create("MDT_APPLIST_DCTM_ERROR","Applist likely not setup for MdtApplicationContext %s.applist",MdtApplicationContext,dfe);
    	}
    }



    public void onSelectApplicationFromDropDown(DropDownList dropdownlist, ArgumentList arg)
    {
    }



    String[] getMdtApplicationListFromPreferenceRepository()
    {        
        /*-INFO-*/Lg.inf("look up webtop's applicatoin list from repository");
        /*-dbg-*/Lg.dbg("get application context from component config");
    	String MdtApplicationContext = this.lookupString("MdtApplistFile");
        /*-dbg-*/Lg.dbg("  app context: %s",MdtApplicationContext);
    	try {
	        /*-dbg-*/Lg.dbg("get preference repository instance");
	        PreferenceRepository prefrep = PreferenceRepository.getInstance();
	        /*-dbg-*/Lg.dbg("get SESSION from pref repository session manager");
	        IDfSession prefsess = prefrep.getRepositorySession();
	        //password = TrustedAuthenticatorUtils.decryptByDES(password);
	        /*-dbg-*/Lg.dbg("  SESSION docbase: %s",prefsess.getDocbaseName());
	        /*-dbg-*/Lg.dbg("  SESSION user: %s",prefsess.getLoginUserName());
	        /*-dbg-*/Lg.dbg("get applist configuration sysobject");
	        String qual = "dm_sysobject where object_name = '"+MdtApplicationContext+".applist'";
	        /*-dbg-*/Lg.dbg("  qual: %s",qual);
	    	IDfSysObject applistdocument = (IDfSysObject)prefsess.getObjectByQualification(qual);
	        /*-dbg-*/Lg.dbg("  sysobject: %s",applistdocument);
	        /*-dbg-*/Lg.dbg("take keywords as applist - iterate...");
	    	List applicationlist = new ArrayList();
	    	for (int i=0; i < applistdocument.getValueCount("keywords"); i++)
	    	{
		        /*-dbg-*/Lg.dbg("  -iteration #%d",i);
	    		String appname = applistdocument.getRepeatingString("keywords", i);
		        /*-dbg-*/Lg.dbg("  -add appname %s",appname);
	    		applicationlist.add(appname);
	    	}
	        /*-dbg-*/Lg.dbg("convert list to string array");
	   	 	String[] applist = new String[applicationlist.size()];
	      	for (int i=0; i <applicationlist.size(); i++) {applist[i] = (String)applicationlist.get(i);}
	        /*-dbg-*/Lg.dbg("returning application list");
	    	return applist;

    	} catch (DfServiceException dfse) {
            /*-WARN-*/Lg.wrn("Error defaulting MDT application list dropdown, Global Registry is probably not setup correctly",dfse);
			throw EEx.create("MDT_APPLIST_SERVICE_ERROR","Global Registry likely down or not configured",dfse);
    	} catch (DfException dfe) {
            /*-WARN-*/Lg.wrn("Error defaulting MDT application list dropdown, document probably not found",dfe);
			throw EEx.create("MDT_APPLIST_DCTM_ERROR","Applist likely not setup for MdtApplicationContext %s.applist",MdtApplicationContext,dfe);
    	}
    }



    String[] getMdtApplicationListFromComponentXML()
    {
        /*-INFO-*/Lg.inf("look up webtop's applicatoin list - get MdtApplications config element");
    	IConfigElement cfe = this.lookupElement("MdtApplications");
        /*-dbg-*/Lg.dbg("  getting child elements");
    	Iterator iter = cfe.getChildElements();
    	List applicationlist = new ArrayList();
        /*-dbg-*/Lg.dbg("  iterating...");
    	while (iter.hasNext()) {
            /*-dbg-*/Lg.dbg("  get next element");
    		IConfigElement curelement = (IConfigElement)iter.next();
            /*-dbg-*/Lg.dbg("  get element value");
    		String curappname = curelement.getValue();
            /*-dbg-*/Lg.dbg("  add application %s to list",curappname);
    		applicationlist.add(curappname);
    	}
        /*-dbg-*/Lg.dbg("convert app list to array");
   	 	String[] applist = new String[applicationlist.size()];
      	for (int i=0; i <applicationlist.size(); i++) {applist[i] = (String)applicationlist.get(i);}
    	return applist;
    }


}
