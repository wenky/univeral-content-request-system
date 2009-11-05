package com.medtronic.ecm.documentum.core.webtop;

import java.util.List;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.webcomponent.library.contenttransfer.importcontent.ImportContent;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;

public class MdtImportContent extends ImportContent
{
	ScrollableResultSet mdtTypesResultSet_ = null;

    protected ScrollableResultSet createTypesResultSet()
    {
        /*-INFO-*/Lg.inf("Top - get the list of types for the logged in application");
        if (mdtTypesResultSet_ != null) {
            /*-dbg-*/Lg.dbg("returning cached resultset");
            return mdtTypesResultSet_;            
        }
        TableResultSet resultSet;        
        /*-dbg-*/Lg.dbg("create new resultset");
        resultSet = new TableResultSet(new String[] {
            "type_name", "label_text"
        });
        
        try { 
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
    	mdtTypesResultSet_ = resultSet;
        return resultSet;
    	
    }
    
    
    
    // this gets called at strange times, usually to set the type to dm_document when we don't want it to.
    // override attempts to set the type to the first type in the usual types result set, or otherwise 
    // returns super.getDocbaseType()
    protected String getDocbaseType()
    {
        /*-INFO-*/Lg.inf("Top");
        /*-dbg-*/Lg.dbg("check if types resultset is already cached/computed/retrieved");
    	if (mdtTypesResultSet_ == null)
    	{
            /*-dbg-*/Lg.dbg("retrieving types result set");
    		createTypesResultSet();
    	}
		// get ?first? one
        /*-dbg-*/Lg.dbg("try to reset and get the first row's type");
        try { 
	    	mdtTypesResultSet_.setCursor(-1);
	        /*-dbg-*/Lg.dbg("are there any rows?");
	    	if (mdtTypesResultSet_.next()) {
	            /*-dbg-*/Lg.dbg("rows found, getting column number for 'type_name'");
	    		int nTypeNameColumn = mdtTypesResultSet_.findColumn("type_name");
	            /*-dbg-*/Lg.dbg("getting column #%s",nTypeNameColumn+1);
	    		String type = (String)mdtTypesResultSet_.getObject(nTypeNameColumn+1);
	            /*-dbg-*/Lg.dbg("returning type: "+type);
	    		return type;
	    	} else { 
	            /*-dbg-*/Lg.dbg("no types found, returning super");
	    		String defaulttype = super.getDocbaseType();
	            /*-dbg-*/Lg.dbg("returning default type %s",defaulttype);
	    		return defaulttype;
	    	}
        } catch (Exception e) {
            /*-WARN-*/Lg.wrn("Exception occurred trying to get result type list, trying to return super's default type");
    		String defaulttype = super.getDocbaseType();
            /*-dbg-*/Lg.dbg("returning default type %s from exception handler",defaulttype);
    		return defaulttype;
        	
        }
    }
    


}
