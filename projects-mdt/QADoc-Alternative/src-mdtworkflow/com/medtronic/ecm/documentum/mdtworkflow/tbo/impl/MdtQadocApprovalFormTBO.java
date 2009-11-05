package com.medtronic.ecm.documentum.mdtworkflow.tbo.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPolicy;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfDynamicInheritance;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;
import com.medtronic.ecm.documentum.core.tbo.impl.MdtBaseDocumentTBO;
import com.medtronic.ecm.documentum.mdtworkflow.tbo.IMdtQadocApprovalFormTBO;
import com.medtronic.ecm.documentum.qad.plugins.IMdtSignaturePropertiesPlugin;
import com.medtronic.ecm.util.CollUtils;

public class MdtQadocApprovalFormTBO extends MdtBaseDocumentTBO implements IMdtQadocApprovalFormTBO
{
    
    public void attachLifecycle() throws DfException
    {
        /*-INFO-*/Lg.inf("attaching lifecycle - %s",this);
        /*-dbg-*/Lg.dbg("getting session manager reference and docbase");
        IDfSessionManager smgr = this.getSessionManager();
        String docbase = this.getSession().getDocbaseName();
        /*-dbg-*/Lg.dbg("get doctype/appname");
        String doctype = this.getTypeName();
        /*-dbg-*/Lg.dbg("doc type: %s",doctype);
        String appname = this.getString("m_application");
        /*-dbg-*/Lg.dbg("app name: %s",appname);
        /*-dbg-*/Lg.dbg("get app's configuration from ConfigService");
        MdtConfigService cs = MdtConfigService.getConfigService(smgr, docbase);
        /*-dbg-*/Lg.dbg("get lc name from configuration");
        String lifecyclename = (String)CollUtils.lookup(cs.getAppConfig(appname),"DocumentTypes",doctype,"Lifecycle");
        /*-dbg-*/Lg.dbg("look for optional alias set attachment");
        String aliasset = (String)CollUtils.lookup(cs.getAppConfig(appname),"DocumentTypes",doctype,"LifecycleAlias");
        /*-dbg-*/Lg.dbg("aliasset: %s - making empty string if null...",aliasset);
        if (aliasset == null) aliasset = "";
        /*-dbg-*/Lg.dbg("compose qualification search clause for lifecycle %s",lifecyclename);
        String lcqualification = "dm_policy where object_name = '"+lifecyclename+"'"; 
        /*-dbg-*/Lg.dbg("retrieve lc object with qualification %s",lcqualification);
        IDfPolicy lifecycle = (IDfPolicy)getSession().getObjectByQualification(lcqualification);
        /*-dbg-*/Lg.dbg("attaching policy at base state");
        this.attachPolicy(lifecycle.getObjectId(), "0", aliasset);
        /*-dbg-*/Lg.dbg("lc attached");
    }
    
    public void generateObjectName() throws DfException
    {
        /*-INFO-*/Lg.inf("generating object name - %s",this);
        /*-dbg-*/Lg.dbg("getting session manager reference and docbase");
        IDfSessionManager smgr = this.getSessionManager();
        String docbase = this.getSession().getDocbaseName();
        /*-dbg-*/Lg.dbg("get doctype/appname");
        String doctype = this.getTypeName();
        /*-dbg-*/Lg.dbg("doc type: %s",doctype);
        String appname = this.getString("m_application");
        /*-dbg-*/Lg.dbg("app name: %s",appname);
        /*-dbg-*/Lg.dbg("get app's configuration from ConfigService");
        MdtConfigService cs = MdtConfigService.getConfigService(smgr, docbase);
        /*-dbg-*/Lg.dbg("get naming plugin list from configuration");
        List namingplugins = (List)CollUtils.lookup(cs.getAppConfig(appname),"DocumentTypes",doctype,"NamingPlugins");                
        /*-dbg-*/Lg.dbg("generate name with plugins");
        String name = "";
        /*-dbg-*/Lg.dbg("iterate through naming");
        IDfSession session = getSession();
        /*-trc-*/Lg.trc("get DfClient in preparation for SBO invocations");
        IDfClient dctmclient = session.getClient();
        /*-trc-*/long[]start=null,finish=null;if(Lg.trc()){start=new long[namingplugins.size()];finish=new long[namingplugins.size()];}
        for (int i=0; i < namingplugins.size(); i++) 
        {
            /*-dbg-*/Lg.dbg("exec plugin #%d",i);
            MdtPlugin curplugin = (MdtPlugin)namingplugins.get(i);              
            /*-dbg-*/Lg.dbg("plugin class: %s",curplugin != null ? curplugin.classname : "NULL plugin");
            /*-trc-*/if(Lg.trc())start[i]=(new Date()).getTime();           
            Object sboobj = MdtPluginLoader.loadPlugin(curplugin,session.getSessionManager());
            /*-trc-*/try{if(Lg.trc()){Class[] ifaces = sboobj.getClass().getInterfaces();Class c = IMdtDocumentNaming.class;Lg.trc("iface class: %d casting class: %d equals? %s",ifaces[0],IMdtDocumentNaming.class,ifaces[0]==IMdtDocumentNaming.class);}}catch(Exception e) {}
            IMdtDocumentNaming namer = (IMdtDocumentNaming)sboobj;
            name += namer.generateName(session.getDocbaseName(),this, curplugin.context);
            /*-trc-*/if(Lg.trc())finish[i]=(new Date()).getTime();          
            /*-dbg-*/Lg.dbg("cur val of name: [%s]",name);
        }
        /*-trc-*/try{if(Lg.trc())for(int jj=0;jj<namingplugins.size();jj++)Lg.trc("Naming SBO %d - start %d finish %d",jj,start[jj],finish[jj]);}catch(Throwable t){}
        /*-dbg-*/Lg.dbg("setting object name");
        setObjectName(name);
//      /*-dbg-*/MdtLog.dbg("setting backup name");
//      setString(MDT_SYSTEMNAME_ATTR_NAME,name);       
    }
    
    // if no checkin modes or lifecycle or state definition is found, we assume that checkin controls aren't in effect for the given lifecycle and state.
    // ACL controls are assumed to be covering the situation (i.e. editing and versioning is locked down). Superusers also are not interfered with. 
    public IDfId DEPRECATEDdoCheckin(boolean fRetainLock, String versionLabels, String oldCompoundArchValue, String oldSpecialAppValue, String newCompoundArchValue, String newSpecialAppValue, Object[] extendedArgs) throws DfException 
    {
        ///*-INFO-*/Lg.logMethodEntry(null, versionLabels);
        /*-dbg-*/Lg.dbg("check if SU");
        if (isSuperuser()) {
            // allowed/do nothing.
            /*-dbg-*/Lg.dbg("is SU, do not interfere");
            return super.doCheckin(fRetainLock, versionLabels, oldCompoundArchValue, oldSpecialAppValue, newCompoundArchValue, newSpecialAppValue, extendedArgs);
        } else {
            /*-dbg-*/Lg.dbg("version labels: %s",versionLabels);            
            // get acceptable checkin modes
            /*-dbg-*/Lg.dbg("not su, get allowable modes");
            List modes = getAllowableCheckinModes();
            if(modes != null) {
                if (versionLabels == null || "".equals(versionLabels.trim())) {
                    /*-dbg-*/Lg.dbg("controlled checkin modes, labels must be defined...");
                    throw EEx.create("QADocTBO-Checkin-nolabels","Invalid Checkin - controlled checkin could not determine checkin type");
                }
                /*-dbg-*/Lg.dbg("modes defined, scanning");
                for (int i=0; i < modes.size(); i++)
                {
                    if (versionLabels.contains((String)modes.get(i))) {
                        /*-dbg-*/Lg.dbg("versionlabels %s matched mode %s",versionLabels,modes.get(i));
                        return super.doCheckin(fRetainLock, versionLabels, oldCompoundArchValue, oldSpecialAppValue, newCompoundArchValue, newSpecialAppValue, extendedArgs);                       
                    }
                }
                // we've gotten here, which isn't good, no mode match. Throw Exception!
                /*-dbg-*/Lg.dbg("controlled checkin mode not matched with versionlabels");
                throw EEx.create("QADocTBO-Checkin-nolabels","Invalid Checkin - controlled checkin could not determine checkin type");
            } else {
                /*-dbg-*/Lg.dbg("no acceptable modes defined, proceed without interference");
                return super.doCheckin(fRetainLock, versionLabels, oldCompoundArchValue, oldSpecialAppValue, newCompoundArchValue, newSpecialAppValue, extendedArgs);
            }
        }
    }   
    
    protected List getAllowableCheckinModes() throws DfException
    {
        // check if checkin controls exist 
        IDfSessionManager smgr = this.getSessionManager();
        /*-dbg-*/Lg.dbg("getting app config");
        String docbase = this.getSession().getDocbaseName();
        /*-dbg-*/Lg.dbg("get doctype/appname");
        String doctype = this.getTypeName();
        /*-dbg-*/Lg.dbg("doc type: %s",doctype);
        String appname = this.getString("m_application");
        /*-dbg-*/Lg.dbg("app name: %s",appname);
        MdtConfigService cs = MdtConfigService.getConfigService(smgr, docbase);
        /*-dbg-*/Lg.dbg("get lcname");
        String lcname = this.getPolicyName();
        /*-dbg-*/Lg.dbg("  --lc: %s",lcname);
        /*-dbg-*/Lg.dbg("get state");
        String statename = this.getCurrentStateName();
        /*-dbg-*/Lg.dbg("  --state: %s",statename);
        
        /*-dbg-*/Lg.dbg("get app's configuration from ConfigService");
        Map config = (Map)cs.getAppConfig(appname);
        try { 
            /*-dbg-*/Lg.dbg("get lc defs");
            Map lifecycles = (Map)config.get("Lifecycles");
            /*-dbg-*/Lg.dbg("get lc for docment");
            Map lcycle = (Map)lifecycles.get(lcname);
            /*-dbg-*/Lg.dbg("get state defs");
            Map states = (Map)lcycle.get("States");
            /*-dbg-*/Lg.dbg("get state");
            Map statedef = (Map)states.get(statename);
            /*-dbg-*/Lg.dbg("get checkin config for state");
            Map checkininfo = (Map)statedef.get("Checkin");
            /*-dbg-*/Lg.dbg("get state list");
            List allowablemodes = (List)checkininfo.get("AllowableModes");
            return allowablemodes;
        } catch (NullPointerException npe) {
            /*-dbg-*/Lg.dbg("state %s for lifecycle %s not configured for checkin controls, returning null",statename,lcname);          
            return null;
        }       
    }
    
    public String getCustomSigningProperties(String username, String justification, String signaturemethod,String appprops, String passthru1,String passthru2)
    {
        try { 
            // check for custom esignature logic 
            /*-dbg-*/Lg.dbg("custom esignature TBO method called");
            IDfSessionManager smgr = this.getSessionManager();
            String docbase = this.getSession().getDocbaseName();
            /*-dbg-*/Lg.dbg("getting configservice for docbase: %s",docbase);
            MdtConfigService cs = MdtConfigService.getConfigService(smgr, docbase);
            /*-dbg-*/Lg.dbg("get mdt app");
            String appname = getString(MDT_APPLICATION_ATTR_NAME);
            /*-dbg-*/Lg.dbg("--mdtapp: %s",appname);
            Map appcfg = (Map)cs.getAppConfig(appname);
            // do app-global for now, an app-global can further subdivide if necessary
            /*-dbg-*/Lg.dbg("check for custom signature properties plugins");
            if (appcfg.containsKey("CustomSignatureProperties"))
            {
                /*-dbg-*/Lg.dbg("found! execute custom signature properties layer - get tbo session");
                IDfSession session = this.getSession();
                
                // signature properties execute in a pipeline/streaming format. a hashmap is sent to each
                // plugin, which updates it, and then returns the map. That map is then sent to the next
                // one, allowing them to "build up" the map. At the end, the contents are enumerated and
                // serialized into a csv of key-values. It is assumed the keys will be strings and the 
                // values will be strings or computable by toString()...
                List signaturepropertiespluginlist = (List)appcfg.get("CustomSignatureProperties");
                Map propertiesmap = new HashMap();
                for (int i=0; i < signaturepropertiespluginlist.size(); i++) 
                {
                    /*-dbg-*/Lg.dbg("exec plugin #%d",i);
                    MdtPlugin curplugin = (MdtPlugin)signaturepropertiespluginlist.get(i);              
                    /*-dbg-*/Lg.dbg("plugin class: %s",curplugin != null ? curplugin.classname : "NULL plugin");
                    Object sboobj = MdtPluginLoader.loadPlugin(curplugin,session.getSessionManager());
                    IMdtSignaturePropertiesPlugin propplugin = (IMdtSignaturePropertiesPlugin)sboobj;
                    /*-dbg-*/Lg.dbg("loaded, exec");
                    propplugin.getProperties(this,propertiesmap, username, justification, signaturemethod, appprops, passthru1, passthru2, curplugin.context);
                }

                // now, serialize and return
                /*-dbg-*/Lg.dbg("iterate property map");
                Iterator keys = propertiesmap.keySet().iterator();
                // stringbuffers are faster, but this isn't batch processing
                String csv = null;
                while (keys.hasNext())
                {
                    /*-dbg-*/Lg.dbg("Next Property...");
                    csv = (csv == null ? "" : csv+",");
                    Object key = keys.next();
                    Object value = propertiesmap.get(key);
                    if (value != null && !"".equals(value)) {
                        String newkey = key.toString()+'='+value.toString();
                        /*-dbg-*/Lg.dbg("newkey: %s",newkey);
                        // csv encoding ? strip commas? does it handle proper CSV-standardized encoding?
                        csv += newkey;
                    }
                }
                /*-dbg-*/Lg.dbg("returning csv string: %s",csv);
                return csv;
            }
        } catch (Exception e) {
            // To throw or not throw...? Is this "required" or "nice to have?"
            /*-ERROR-*/Lg.err("Exception in signature properties calculation",e);            
        }
        // default if no defintion: return what was given
        return appprops;
    }

    public String getVersion() { return "6.6.6";}
    public String getVendorString() {return "MDT-Workflow-Form";}
    public boolean isCompatible(String arg0) { return true; }
    public boolean supportsFeature(String f) { return true; }

        
}

