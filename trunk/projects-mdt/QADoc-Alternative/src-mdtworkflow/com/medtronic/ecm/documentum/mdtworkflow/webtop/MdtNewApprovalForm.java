package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.Iterator;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfValue;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.library.create.NewDocument;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;
import com.medtronic.ecm.util.Is;

public class MdtNewApprovalForm extends NewDocument
{
    protected String m_inittype = null; // set by getTypeComboResultSetFromMdtConfig...
    public void onInit(ArgumentList arg)
    {
        /*-INFO-*/Lg.inf("Top - CALL NewDocument.onInit()");
        Iterator i = arg.nameIterator(); 
        /*-dbg-*/if(Lg.dbg())while (i.hasNext()) { String argname = (String)i.next();Lg.dbg(argname + " : " + arg.get(argname));}
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

        Text objectname = (Text)getControl("attribute_object_name", Text.class);
        if (Is.empty(objectname.getValue())) {
            objectname.setValue("New Form");
        }
        
        //DataDropDownList formatList = (DataDropDownList)getControl("formatList", DataDropDownList.class);
        //formatList.setEnabled(false);
        
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
            /*-dbg-*/Lg.dbg("iterate on medtronic config to get union of [change request] type lists");
            for (int i=0; i < applist.size(); i++) 
            {
                /*-dbg-*/Lg.dbg("get next mdtapp");
                String application = (String)applist.get(i);
                /*-dbg-*/Lg.dbg("get CHANGE REQUEST type list for application %s",application);
                List changerequesttypes = WorkflowUtils.getChangeRequestTypesForApplication(getDfSession(),cfg,application);
                /*-dbg-*/Lg.dbg("add types to result set");
                for (int j=0; j < changerequesttypes.size(); j++) 
                {
                    String typename = (String)changerequesttypes.get(j);
                    if (j==0) m_inittype = typename;
                    /*-dbg-*/Lg.dbg("lookup type definition for %s",typename);
                    IDfType typeinfo = getDfSession().getType(typename);
                    /*-dbg-*/Lg.dbg("get label for %s",typeinfo);
                    String typelabel = typeinfo.getDescription() + " ("+typename+')';
                    /*-dbg-*/Lg.dbg("adding type %s label %s",typename,typelabel);
                    resultSet.add(new String[] {typename,typelabel} );
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
    
    public void onSelectType(DataDropDownList typeList, ArgumentList arg)
    {
        super.onSelectType(typeList,arg);
        DataDropDownList formatList = (DataDropDownList)getControl("formatList", DataDropDownList.class);
        String selectedtype = typeList.getValue();
        //getFormats(selectedtype,formatList);
        // fake selection of html format
        //formatList.setValue("html");
        String formatlistvalue = formatList.getValue(); 
        if ("".equals(formatlistvalue)) {
            String defaultFormat = getDefaultFormat(formatList);
            if (defaultFormat != null) {
                formatList.setValue(defaultFormat);
                onSelectFormat(formatList, arg);
            }
        }
        
    }
    
    public void onSelectFormat(DataDropDownList formatList, ArgumentList arg)
    {
        String formatlistvalue = formatList.getValue(); 
        if ("".equals(formatlistvalue)) {
            String defaultFormat = getDefaultFormat(formatList);
            if (defaultFormat != null) {
                formatList.setValue(defaultFormat);
            }
        }
        super.onSelectFormat(formatList, arg);        
    }



    public boolean createNewObject() {
        // if documents were selected in the New Approval Form action, then preset/autopopulate those documents
        // in the appropriate property
        // CEM: currently, the plan is to hide m_attachments and use the attachments tab for all attachment management.
        boolean result = super.createNewObject(); 
        try { 
            if (result) {
                // this document will be already checked out. can you save while it's checked out?
                IDfSysObject newObj = (IDfSysObject)getDfSession().getObject(new DfId(getNewObjectId()));
                // autoset the
                return true;
            }
        } catch (DfException dfe) {
        }
        return false;
    }
    
    
    String getDefaultFormat(DataDropDownList formatList)
    {
        List options = formatList.getOptions();
        if (options.size() > 0) {
            for (int i=0; i < options.size(); i++)
            {
                Option opt = (Option)options.get(i);
                String value = opt.getValue();
                if (!"".equals(value))
                    return value;
            }
        } else { 
            DataDropDownList typedropdown = (DataDropDownList)getControl("objectTypeList", DataDropDownList.class);
            String strSelectedType = typedropdown.getValue();
            // get it via dql
            IDfCollection c = null; 
            try {
                String dql = 
                    "SELECT name, description FROM dm_format WHERE " +
                        "name IN (SELECT a_content_type FROM dm_sysobject WHERE FOLDER('/Templates', DESCEND) AND r_object_type = '"+strSelectedType+"') " +
                        "AND is_hidden=0 ORDER BY description";

                IDfQuery query = query = DfcUtils.getClientX().getQuery();
                query.setDQL(dql);
                c = query.execute(getDfSession(), query.DF_READ_QUERY);
                if (c.next()) {
                    IDfValue retval = c.getValueAt(0);
                    return retval.asString();
                }
            } catch (Exception e) {
                throw new WrapperRuntimeException(e);
            } finally {
                try {c.close();}catch(Exception e){}
            }
            
        }
        
        return null;
        
    }

    
}

