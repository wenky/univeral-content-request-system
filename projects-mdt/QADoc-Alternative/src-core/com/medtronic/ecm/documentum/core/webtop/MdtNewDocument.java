package com.medtronic.ecm.documentum.core.webtop;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.webcomponent.library.create.NewDocument;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;

public class MdtNewDocument extends NewDocument
{
	protected String m_inittype = null; // set by getTypeComboResultSetFromMdtConfig...
    public void onInit(ArgumentList arg)
    {
        /*-INFO-*/Lg.inf("Top - CALL NewDocument.onInit()");    	
        super.onInit(arg);
        // override the type list with a different set of options
        /*-dbg-*/Lg.dbg("get medtronic type list");
        ScrollableResultSet mdttypelist = getTypeComboResultSetFromMdtConfig();
        /*-dbg-*/Lg.dbg("get type list dropdown control");
        DataDropDownList typedropdown = (DataDropDownList)getControl("objectTypeList", DataDropDownList.class);
        /*-dbg-*/Lg.dbg("set dropdown's resultset/typelist to the MdtConfig's list");
        typedropdown.getDataProvider().setResultSet(mdttypelist, null);
        // trigger handlers
        /*-dbg-*/Lg.dbg("simulate a type selection - set dropdown widget object value to %s (initvalue)",m_inittype);
        typedropdown.setValue(m_inittype);
        /*-dbg-*/Lg.dbg("fire event onSelectType",m_inittype);
        onSelectType(typedropdown,new ArgumentList());
        /*-dbg-*/Lg.dbg("done with init");
    }
        
    public ScrollableResultSet getTypeComboResultSetFromMdtConfig()
    {
        /*-INFO-*/Lg.inf("Top - get the list of types for the logged in application");    	
        TableResultSet resultSet;
        /*-dbg-*/Lg.dbg("create new resultset");
        resultSet = new TableResultSet(new String[] {
            "type_name", "label_text"
        });
        
        try { 
            // determine current mdtapp group membership - this could be multiple applications for things like MDAM...
            /*-dbg-*/Lg.dbg("get application codes to determine selected Medtronic application context");            
            MdtConfigService cfg = MdtConfigService.getConfigService(getDfSession().getSessionManager(), getDfSession().getDocbaseName());
            /*-dbg-*/Lg.dbg("get current user");
            String currentusername = getDfSession().getLoginUserName();
            /*-dbg-*/Lg.dbg("get applist for user %s",currentusername);
            List applist = cfg.getMdtApplicationsFromGroupMembership(currentusername);            
            if (applist == null || applist.size() < 1) {
                /*-ERROR-*/Lg.err("User %s is not registered to any medtronic applications",currentusername);
                throw EEx.create("GetTypes-GetApp","User is not registered to any medtronic applications",currentusername);
            }
	        // get filtered list based on dynamic group/application
            /*-dbg-*/Lg.dbg("iterate on medtronic config to get union of type lists");
            for (int i=0; i < applist.size(); i++) 
            {
	            /*-dbg-*/Lg.dbg("get next mdtapp");
            	String application = (String)applist.get(i);
	            /*-dbg-*/Lg.dbg("get type list for application %s",application);
		        List types = cfg.getTypesForApplication(application);
	            /*-dbg-*/Lg.dbg("add types to result set");
		        for (int j=0; j < types.size(); j++) 
		        {
		        	String typename = (String)types.get(j);
		        	if (j==0) m_inittype = typename;
		            /*-dbg-*/Lg.dbg("lookup type definition for %s",typename);
		            IDfType typeinfo = getDfSession().getType(typename);
		            /*-dbg-*/Lg.dbg("get label for %s",typeinfo);
		            String typelabel = typeinfo.getDescription() + " ("+typename+')';
		            /*-dbg-*/Lg.dbg("adding type %s label %s",typename,typelabel);
		            if (!typeinfo.isSubTypeOf("mdt_workflow_form")) {
		                resultSet.add(new String[] {typename,typelabel} );
		            }
		        }
            }
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("DFC error in getting doctype list for application",dfe);
            throw EEx.create("GetTypes-DFE","DFC error in getting doctype list for application",dfe);
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Non-DFC (probably parsing) error in getting doctype list for application",e);
            throw EEx.create("GetTypes-Unknown","Non-DFC (probably parsing) error in getting doctype list for application",e);
        }
        return resultSet;
    	
    }
    


}
