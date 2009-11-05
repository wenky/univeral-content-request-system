package com.medtronic.ecm.documentum.core.tbo.impl;

import com.documentum.fc.client.DfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfDynamicInheritance;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;



/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.12 $
 * 
 */ 
public abstract class MdtBaseDocumentTBO extends DfDocument implements IDfDynamicInheritance
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static String MDT_APPLICATION_ATTR_NAME = "m_application";
    public static String MDT_SYSTEMNAME_ATTR_NAME = "m_systemname";
    public static String MDT_INITIALIZATION_STATUS_ATTR_NAME = "m_init_status";
    
    boolean allowattach = false;

    protected boolean isTemplatesCabinet() throws DfException
    {
        /*-dbg-*/Lg.dbg("lookup cabinet of current primary link");
        IDfId folderid = getFolderId(0);
        IDfSysObject folder = (IDfSysObject)getSession().getObject(folderid);
        IDfId cabid = folder.getCabinetId();
        if (cabid != null)
        {
            IDfSysObject cabinet = (IDfSysObject)getSession().getObject(cabid);
            /*-dbg-*/Lg.dbg("check name of cabinet %s",cabinet);
            String name = cabinet.getObjectName();
            /*-dbg-*/Lg.dbg("see if cabinet name %s is the Templates cabinet");
            if ("Templates".equals(name)) {
                /*-dbg-*/Lg.dbg("returning true");
                return true;
            }
        }
        /*-dbg-*/Lg.dbg("returning false");
        return false;
        
    }
    
    /**
     * Trigger application assignment, name generation, and lifecycle attachment tasks for newly
     * created documents. Documents are typically created from Webtop via these three paths:
     *  1. New Document w/o template:
     *    - save() is called after the type+template and save() called again after properties
     *  2. New Document w/ template:
     *    - saveasnew() is called *on the template object*, and save() is called on the new doc after properties
     *  3. Import:
     *    - unknown. double save? single save?   
     * If New Document without template
     * 
     * @param saveLock <font color="#0000FF"><b>(boolean)</b></font> TODO:
     * @param versionLabel <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param args <font color="#0000FF"><b>(Object[])</b></font> TODO:
     * 
     * @throws DfException
     * 
     * @since 1.0
     * 
     */ 
    public void doSave(boolean saveLock, String versionLabel, Object[] args) throws DfException
    {
		/*-INFO-*/Lg.inf("top - %s",this);
		/*-dbg-*/Lg.dbg("check init status");
		boolean initstatus = getBoolean(MDT_INITIALIZATION_STATUS_ATTR_NAME);
		/*-dbg-*/Lg.dbg("  initstatus: %s, isNew: %s",initstatus,isNew());
    	if (!initstatus) 
    	{
    		/*-dbg-*/Lg.dbg("check not superuser (SU bypasses name generate + LC attach + setting init flag)");
    		if (!isSuperuser()) {
	        	// set application value
	    		/*-dbg-*/Lg.dbg("doc uninited - getting session manager reference and docbase");
	    		IDfSessionManager smgr = this.getSessionManager();
	    		String docbase = this.getSession().getDocbaseName();
	    		/*-dbg-*/Lg.dbg("getting configservice for docbase: %s",docbase);
	    		MdtConfigService cs = MdtConfigService.getConfigService(smgr, docbase);
	    		/*-dbg-*/Lg.dbg("prepare to look up app name");
	    		String doctype = this.getTypeName();
	    		/*-dbg-*/Lg.dbg("doc type: %s",doctype);
	    		String appname = cs.getMdtApplicationName(doctype);
	    		/*-dbg-*/Lg.dbg("setting appname attribute to resolved app name: %s",appname);
	    		this.setString(MDT_APPLICATION_ATTR_NAME,appname);
                /*-dbg-*/Lg.dbg("check if template");
	    		boolean istemplate = isTemplatesCabinet(); 
                /*-dbg-*/Lg.dbg("istemplate? %b",istemplate);
	    		if (!istemplate) {
    	    		/*-dbg-*/Lg.dbg("setting init flag to T");
    	    		this.setBoolean(MDT_INITIALIZATION_STATUS_ATTR_NAME,true);
            		/*-dbg-*/Lg.dbg("invoke name generation hook");
        			generateObjectName();
	    		}
        		/*-dbg-*/Lg.dbg("post name, pre LC intermediate save");
        		super.doSave(saveLock,versionLabel,args);
        		if (!istemplate) {
            		/*-dbg-*/Lg.dbg("apply LC");
            		allowattach = true;
            		attachLifecycle();
            		allowattach = false;
	    		}
    		}
    	} else {    		
//    		/*-dbg-*/MdtLog.dbg("ensure integrity of object name");
//    		if (!isSuperuser()) {
//    			restoreSystemName();
//    		}
    	}
		/*-dbg-*/Lg.dbg("calling super.save()");
    	super.doSave(saveLock,versionLabel,args);
		/*-dbg-*/Lg.dbg("super.save() completed without error");
    }
    
    /**
     *
     * Currently not used, but it is possible to prevent changes to the system name by comparing a 
     * backup of the initial object name, and if the actual differs, overwriting it at the next save.
     * This functionality was decided to not be used, but it's being kept here just in case...
     * 
     * @throws DfException
     * 
     * @since 1.0
     *  
     * @throws DfException
     */
    public void restoreSystemName() throws DfException
    {
		String backupname = getString(MDT_SYSTEMNAME_ATTR_NAME);
		/*-dbg-*/Lg.dbg("not superuser, so check if change was made");
		if (getObjectName() == null || !getObjectName().equals(backupname))
		{
			// restore system name
    		/*-dbg-*/Lg.dbg("restoring object name to %s",backupname);
			setObjectName(backupname);
		}
    	
    }
    
    /**
     * This simply tracks the invocation of doSaveAsNew for debugging and tracing purposes. The sequence 
     * of invocation of save and saveasnew() can vary by whether a document is created via New Document w/o template, 
     * New Document WITH template, or an Import.  
     * 
     * @param shareContent <font color="#0000FF"><b>(boolean)</b></font> TODO:
     * @param copyRelations <font color="#0000FF"><b>(boolean)</b></font> TODO:
     * @param extendedArgs <font color="#0000FF"><b>(Object[])</b></font> TODO:
     * @return <font color="#0000FF"><b>IDfId</b></font> - TODO:
     * 
     * @throws DfException
     * 
     * @since 1.0
     * 
     */ 
    public IDfId doSaveAsNew(boolean shareContent, boolean copyRelations, Object[] extendedArgs) throws DfException
    {
		/*-INFO-*/Lg.inf("top - %s",this);
		IDfId newcopy = super.doSaveAsNew(shareContent, copyRelations, extendedArgs);
		/*-dbg-*/Lg.dbg("cloned id: %s",newcopy.getId());
		return newcopy;
    }
    
    /**
     * Ensures enforcement of medtronic application lifecycle control. LCs can only be detached by superusers. 
     * Other attempts will be ignored with a WARN message, but will not throw and exception.
     * 
     * @param extendedArgs <font color="#0000FF"><b>(Object[])</b></font> TODO:
     * 
     * @throws DfException
     * 
     * @since 1.0
     * 
     */ 
    public void doDetachPolicy(Object[] extendedargs) throws DfException
    {
		/*-INFO-*/Lg.inf("top - %s",this);
		/*-dbg-*/Lg.dbg("check if superuser (only superusers may detach)");
    	if (isSuperuser())
    	{
    		/*-dbg-*/Lg.dbg("detaching...");
    		super.doDetachPolicy(extendedargs);
    	} else {
    		// do nothing? 
    		// throw exception?
    		/*-WARN-*/Lg.wrn("NOT superuser, policy/lifecycle detach not allowed");
    	}
		/*-dbg-*/Lg.dbg("done");
    }
    
    /**
     * 
     * Attaches a policy, under Medtronic policy attachment controls/enforcement. You should only be allowed to
     * set the policy of a document IF:
     * - it is new and hasn't been assigned one yet (in initial document creation)
     * - you are superuser
     * Otherwise, it will ignore the attachment attempt, and log a WARN message. It will not throw an exception.
     * 
     * @param policyId <font color="#0000FF"><b>(IDfId)</b></font> TODO:
     * @param stateNameOrIndex <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param scope <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param extendedArgs <font color="#0000FF"><b>(Object[])</b></font> TODO:
     * 
     * @throws DfException
     * 
     * @since 1.0
     * 
     */ 
    public void doAttachPolicy(IDfId policyId, String stateNameOrIndex, String scope, Object[] extendedArgs) throws DfException
    {
		/*-INFO-*/Lg.inf("top - policyID: %s  state: %s  scope: %s object: %s",policyId.getId(),stateNameOrIndex,scope,this);
		/*-dbg-*/Lg.dbg("check if attachment allowed (allowattach is true or user is SU)");
    	if (isSuperuser()) {
    		/*-dbg-*/Lg.dbg("user is superuser - attach allowed");
    		super.doAttachPolicy(policyId,stateNameOrIndex,scope,extendedArgs);
    		/*-dbg-*/Lg.dbg("attached");
    	} else if (allowattach) {
    		/*-dbg-*/Lg.dbg("allowattach was set - attach allowed");
    		super.doAttachPolicy(policyId,stateNameOrIndex,scope,extendedArgs);
    		/*-dbg-*/Lg.dbg("attached");
    	}else {
    		// do nothing? 
    		// throw exception?
    		/*-WARN-*/Lg.wrn("NOT superuser, policy/lifecycle attach not allowed");
    	}
		/*-dbg-*/Lg.dbg("done");
    	
    }
    
    /**
    *
    * This is the base TBO's attachLifecycle method, that is called by the TBO doSave event hook. 
    * It doesn't do anything in the base implementation, the app-specific TBOs should have an 
    * implementation/override of this method that knows how to assign the lifecycle. For example, 
    * QADoc's TBO finds the lifecycle of the doctype specified in an MdtConfigService configuration
    * file by Documentum document type.  
    * 
    * @throws DfException
    * 
    * @since 1.0
    *  
    * @throws DfException
    */
    public void attachLifecycle() throws DfException
    {
		/*-INFO-*/Lg.inf("stub method for attaching lifecycle top - %s",this);
    }
    
    /**
    *
    * This is the base TBO's generateObjectName method, that is called by the TBO doSave event hook. 
    * It doesn't do anything in the base implementation, the app-specific TBOs should have an 
    * implementation/override of this method that knows how to generate the object name. For example, 
    * QADoc's TBO finds a list of plugins assigned to the doctype specified in an MdtConfigService configuration
    * file by Documentum document type, and calls them in sequence, building up an object name. 
    * 
    * Note that this method can be used for other object initialization functions, such as MDAM's 0.1 versioning.
    * 
    * @throws DfException
    * 
    * @since 1.0
    *  
    * @throws DfException
    */
    public void generateObjectName() throws DfException
    {
		/*-INFO-*/Lg.inf("stub method for object name generation top - %s",this);
    }
    
    /**
    *
    * Detects if a user is a superuser, in which case most of the enforced behavior of the TBO is bypassed, such 
    * as name generation and lifecycle autoattachment, and prevention of changing the assigned lifecycle.
    * 
    * @return <font color="#0000FF"><b>boolean</b></font> - TODO:
    * @throws DfException
    * 
    * @since 1.0
    *  
    * @throws DfException
    */
    public boolean isSuperuser() throws DfException
    {
		/*-dbg-*/Lg.dbg("get session");
    	IDfSession session = this.getSession();
		/*-dbg-*/Lg.dbg("get username");
    	String currentuser = session.getLoginUserName();
		/*-dbg-*/Lg.dbg("lookup user for %s",currentuser);
    	IDfUser user = session.getUser(currentuser);
		/*-dbg-*/Lg.dbg("check if superuser: %s",user);
		int priv = user.getUserPrivileges(); 
		/*-dbg-*/Lg.dbg("privs: %d",priv);
    	return (priv & IDfUser.DF_PRIVILEGE_SUPERUSER) > 0;    	
    }

    /**
    *
    * Provides a hook for populating renditions with attribute data. This calls getCustomSigningProperties, 
    * which should be overridden in app-specific TBOs to get the appropriate rendition population key/value csv
    * string. 
    * 
    * @return <font color="#0000FF"><b>boolean</b></font> - TODO:
    * @throws DfException
    * 
    * @since 1.0
    *  
    * @throws DfException
    */
    public IDfId doAddESignature (
            String userName, String password, String signatureJustification, String formatToSign, 
            String hashAlgorithm, String preSignatureHash, String signatureMethodName, 
            String applicationProperties, String passThroughArgument1, String passThroughArgument2, Object[] extendedArgs) throws DfException
    {
        String newappprops = getCustomSigningProperties(userName,signatureJustification,signatureMethodName,applicationProperties,passThroughArgument1,passThroughArgument2);
        if (applicationProperties == null || "".equals(applicationProperties))
        {
            // do nothing
        } else {
            if (newappprops == null || "".equals(newappprops)) {
                newappprops = applicationProperties;
            }else {
                newappprops = applicationProperties + ',' + newappprops;
            }
        }
       
        return super.doAddESignature(userName,password,signatureJustification,formatToSign,hashAlgorithm,preSignatureHash,signatureMethodName,newappprops,passThroughArgument1,passThroughArgument2,extendedArgs);
    }

    /**
    *
    * Do-nothing stub implementation. App-specific TBO can override this to provide additional information
    * to rendition requests that will enable attribute data to show up in rendition sigining cover sheets.
    * For example, QADoc implements a series of plugins, defined in the qadoc app's configuration file, 
    * that build up a hashmap of key-value pairs, which are converted to a string of CSV.   
    * 
    * @return <font color="#0000FF"><b>boolean</b></font> - TODO:
    * @throws DfException
    * 
    * @since 1.0
    *  
    * @throws DfException
    */
    public String getCustomSigningProperties(String username, String justification, String signaturemethod,String appprops, String passthru1,String passthru2)
    {
        // Default do-nothing impl, override in app-specific TBO...
        return appprops;
    }
    
    public IDfId doCheckin(boolean fRetainLock, String versionLabels, String oldCompoundArchValue, String oldSpecialAppValue, String newCompoundArchValue, String newSpecialAppValue, Object[] extendedArgs) throws DfException
    {
        int a=1;
        a++;
        
        return super.doCheckin(fRetainLock, versionLabels, oldCompoundArchValue, oldSpecialAppValue, newCompoundArchValue, newSpecialAppValue, extendedArgs);
    }
    
    
}
