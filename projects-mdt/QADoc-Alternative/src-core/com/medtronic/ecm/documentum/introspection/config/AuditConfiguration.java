/*
 * Created on Apr 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: AuditConfiguration.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:36 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


import java.util.Iterator;
import java.util.List;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.ConfigFile;
import com.documentum.web.formext.config.ConfigService;
import com.documentum.web.formext.config.IConfigElement;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AuditConfiguration
{
    public static         String br="<br>",b1="<b>",b2="</b>";

    
    public static String checkMrcsPlugin(String s, MrcsPlugin p)
    {
        if (p == null)
            s+="plugin not specified"+br;
        else 
            try {
                Class.forName(p.PluginClassName);
                s+="plugin "+p.PluginClassName+" is valid"+br;
            } catch (Exception e) {
                s+="plugin is INVALID!!!"+br;
            }
        return s;
    }
    
    public static String checkMrcsPluginList(String s, List plugins)
    {
        if (plugins == null)
        {
            return (s+"no plugins specified"+br);
        }
        for (int i=0; i<plugins.size(); i++)
        {
            s+= "checking plugin #"+i+br;
            s = checkMrcsPlugin(s,(MrcsPlugin)plugins.get(i));
        }
        return s;       
    }
    
    public static String checkSystemType(String s, IDfSession session, String systype)
    {
        try {
            IDfType docsystype = session.getType(systype);
            s+="system type "+docsystype.getName()+" valid"+br;
        } catch (Exception e) {
            s+="system type "+systype+" NOT FOUND!!!!"+br;
        }
        return s;
        
    }
    
    public static String checkACL(String s, IDfSession session, String acl)
    {
        try {
            IDfACL newACL = session.getACL(session.getServerConfig().getString("operator_name"), acl);
            s+="acl "+newACL.getObjectName()+" found"+br;
        } catch (Exception e) {
            s+="acl "+acl+" NOT FOUND!!!!"+br;
        }
        return s;
    }
    
    // here we go!
    public static String auditconfig(IDfSession session, Component component)
    {
        String s = null;
        
        try {
        
            MrcsFolderConfigFactory folderconfig = MrcsFolderConfigFactory.getFolderConfig();
                        
            String docbase = session.getDocbaseName();
            
            s = b1+"AUDIT REPORT OF CONFIGURATION AGAINST DOCBASE: "+session.getDocbaseName()+b2+br+br;
            
            // Iterate through the apps
            IDfClient client = new DfClient();
            
            IDfSessionManager sMgr = null;
            MrcsConfigBroker configbroker = folderconfig.configbroker;
            Iterator applications = configbroker.applications.keySet().iterator();
            while (applications.hasNext())
            {
                String appkey = (String)applications.next();
                MrcsApplication app = (MrcsApplication)configbroker.applications.get(appkey);
                s+= br+b1+"checking "+app.ApplicationName+b2+br;
                if (!appkey.equals(app.ApplicationName))s+="ApplicationName does not equal storage key!!!!"+br;
                s+= "DocBase: "+app.DocBase+"<br>";
                if (app.DocBase.equals(docbase))
                {    
                    // try to switch to its system user
                    try {
                        
                        //create a Session Manager object
                        sMgr = client.newSessionManager();                        
                        
                        DfLoginInfo loginInfoObj = new DfLoginInfo();
                        loginInfoObj.setUser(app.SystemUsername);
                        loginInfoObj.setPassword(app.SystemPassword);
                        loginInfoObj.setDomain(null);
                        sMgr.setIdentity(docbase, loginInfoObj);
                        // get new session as system user
                        session = sMgr.getSession(docbase);
                    } catch( Exception e ) {
                        s+="application system user "+app.SystemUsername+" could not login to docbase "+docbase+br;
                        continue;
                    }                
                    s+="application system user login successful"+br;
                    
                    // check rootfolder gf type lists
                    for (int i=0; i < app.RootFolders.size(); i++)
                    {
                        s+="checking root folder #"+i+br;
                        MrcsRootFolder rf = (MrcsRootFolder)app.RootFolders.get(i);
                        for (int j=0; j<rf.GroupingFolderTypes.size(); j++)
                        {
                            String gftype = (String)rf.GroupingFolderTypes.get(j);
                            if (app.GroupingFolderTypes.containsKey(gftype))
                                s+="gftype "+gftype+" found in config"+br;
                            else
                                s+="gftype "+gftype+" NOT FOUND in config"+br;
                        }
                    }
                    
                    // checking doctypes
                    // get reference to newDocument component and import components so we can validate the Component pages
                    //config->scope->component->pages
                    
                    Iterator doctypes = app.DocumentTypes.keySet().iterator();
                    while (doctypes.hasNext())
                    {
                        String dockey = (String)doctypes.next();
                        MrcsDocumentType doctype = (MrcsDocumentType)app.DocumentTypes.get(dockey);
                        s+="-----------------------------------------------------------------------------------"+br;
                        s+="checking doctype ["+doctype.Name+"]"+br;
                        if (!dockey.equals(doctype.Name)) s+="Document Type Name does not equal storage key!!!"+br;
                        s = checkSystemType(s,session,doctype.DocumentumSystemType);                        
                        s+="checking custom page in new document component"+br;
                        s = validateCustomComponent("newdoccontainer",doctype.Component,s);
                        s+="checking custom page in import document component"+br;
                        s = validateCustomComponent("mrcsimport",doctype.Component,s);
                        s+= "checking component processor plugin: ";
                        s = checkMrcsPlugin(s,doctype.ComponentProcessorPlugin);
                        // scan document creation plugins
                        s+= "checking document post-creation plugins: ";
                        s = checkMrcsPluginList(s,doctype.DocumentCreationPlugins);
                        // audit document allowable formats
                        if (doctype.AllowableFormats != null)
                        {
                            for (int i = 0; i <doctype.AllowableFormats.size(); i++)
                            {
                                s+="Allowable format #"+i+br;
                                MrcsDocumentAllowableFormat format = (MrcsDocumentAllowableFormat)doctype.AllowableFormats.get(i);
                                // don't I need to further restrict the search based on the doc's DCTM systype?
                                String dqlquery = "SELECT name,description,dos_extension FROM dm_format WHERE is_hidden=0 AND name ='"+format.Format+"'";
                                DfQuery dfquery = new DfQuery();
                                dfquery.setDQL(dqlquery);
                                IDfCollection idfcollection = null;
                                try {
                                    idfcollection = dfquery.execute(session, 0); 
                                    if (idfcollection.next())
                                    {
                                        s+= "format "+format.Format+" is valid"+br;
                                        // audit templates for the format
                                        String tmpldql = "SELECT r_object_id, object_name FROM dm_sysobject "+
                                                         "WHERE FOLDER('/Templates',DESCEND) "+
                                                         "AND r_object_type='"+doctype.DocumentumSystemType+"'"+" AND a_content_type='"+format.Format+"'";
                                        DfQuery templatequery = new DfQuery();
                                        templatequery.setDQL(tmpldql);
                                        boolean found = false;
                                        IDfCollection templates = null;
                                        try {
                                            templates = templatequery.execute(session, 0); 
                                            while (templates.next())
                                            {
                                                found = true;
                                                
                                                String templateid = templates.getString("r_object_id");
                                                String templatename = templates.getString("object_name");
                                                s+="&nbsp;&nbsp;template found: ["+templateid+"] "+templatename+br;
                                            }
                                            if (!found)
                                            {
                                                s+="&nbsp;&nbsp;NO TEMPLATES FOUND FOR FORMAT"+br;
                                            }
                                        } catch (DfException dfexception) {
                                            s+= "exception thrown during template search:"+dfexception+br;
                                        } finally {
                                            try {
                                                if (templates != null)
                                                    templates.close();
                                            } catch (DfException dfexception1) {
                                            }
                                        }                                        
                                    }
                                    else s+= "format "+format.Format+" NOT FOUND!"+br;
                                } catch (DfException dfexception) {
                                    s+= "format "+format.Format+" search (Exception thrown: "+dfexception+")"+br;
                                } finally {
                                    try {
                                        if (idfcollection != null)
                                            idfcollection.close();
                                    } catch (DfException dfexception1) {
                                    }
                                }
                            }
                        }
                        // allowable WFs
                        if (doctype.MrcsWFTemplates == null)
                        {
                            s+= "no workflow templates specified"+br;                            
                        }
                        else
                        {
                            for (int ii= 0; ii <doctype.MrcsWFTemplates.size(); ii++)
                            {
                                String wft = (String)doctype.MrcsWFTemplates.get(ii);
                                if (app.MrcsWFTemplates.containsKey(wft))
                                    s+= "WF template "+wft+" found in app's WF Template list"+br;
                                else
                                    s+= "WF template ["+wft+"] NOT FOUND in app's WF Template list"+br;
                                
                            }
                        }
                    }
                    // check grouping folders
                    Iterator gftypes = app.GroupingFolderTypes.keySet().iterator();
                    while (gftypes.hasNext())
                    {
                        String gfkey = (String)gftypes.next();
                        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gfkey);
                        s+="-----------------------------------------------------------------------------------"+br;
                        s+="checking gftype ["+gftype.Name+"]"+br;
                        if (!gfkey.equals(gftype.Name)) s+="Grouping FolderType Name does not equal storage key!!!"+br;
                        // system types
                        s+="checking GF system type"+br;
                        s = checkSystemType(s,session,gftype.DocumentumSystemType);
                        s+="checking subfolder system type"+br;
                        s = checkSystemType(s,session,gftype.SubfolderDocumentumType);
                        // custom pages
                        s+="checking new grouping folder custom page in new folder component"+br;
                        s = validateCustomComponent("mrcscreatefolder",gftype.Component,s);                        
                        s+="checking new subfolder custom page in new folder component"+br;
                        s = validateCustomComponent("mrcscreatefolder",gftype.SubfolderComponent,s);                        
                        // acls
                        s+="checking GF acl: ";
                        s = checkACL(s,session,gftype.GroupingFolderACL);
                        s+="checking subfolder acl: ";
                        s = checkACL(s,session,gftype.SubfolderACL);
                        s+="checking document initial acl: ";
                        s = checkACL(s,session,gftype.DocumentACL);
                        // grouping folder plugins
                        s+="checking gf naming format plugin: ";
                        s = checkMrcsPlugin(s,gftype.GroupingFolderNamingFormat);
                        s+="checking gf creation component processor plugin: ";
                        s = checkMrcsPlugin(s,gftype.ComponentProcessorPlugin);
                        s+="checking gf post-creation plugin list: ";
                        s = checkMrcsPluginList(s,gftype.GroupingFolderCreationPlugins);
                        // subfolder plugins
                        s+="checking subfolder naming format plugin: ";
                        s = checkMrcsPlugin(s,gftype.SubfolderNamingFormat);
                        s+="checking subfolder creation component processor plugin: ";
                        s = checkMrcsPlugin(s,gftype.SubfolderComponentProcessorPlugin);
                        s+="checking subfolder post-creation plugin list: ";
                        s = checkMrcsPluginList(s,gftype.SubfolderCreationPlugins);
                        // allowable document types
                        for (int adt = 0; adt < gftype.AllowableDocumentTypes.size(); adt++)
                        {
                            MrcsGroupingFolderAllowableDocument gfad = (MrcsGroupingFolderAllowableDocument)gftype.AllowableDocumentTypes.get(adt);
                            s+= "----> checking Grouping Folder allowable document type / override for doctype "+gfad.DocumentType+br; 
                            if (app.DocumentTypes.containsKey(gfad.DocumentType))
                            {
                                s+= "Allowable Document Type "+gfad.DocumentType+" found in defined document types set"+br;
                                // check lifecycle
                                if (gfad.Lifecycle == null)
                                    s+= "All MRCS Documents must have an initial lifecycle, NONE SPECIFIED for this doctype"+br;
                                else
                                {
                                    s+= "checking existence of initial lifecycle "+gfad.Lifecycle+br;
                                    IDfSysObject lifecycle = (IDfSysObject)session.getObjectByQualification("dm_sysobject where object_name ='" + gfad.Lifecycle + "'");
                                    if (lifecycle == null)
                                        s+= "--> lifecycle NOT FOUND"+br;
                                    else
                                        s+= "--> lifecycle object found"+br;
                                }
                                    
                                // check acl override
                                if (gfad.DocumentACL != null)
                                {
                                    s+= "checking validity of document ACL *override*"+gfad.DocumentACL+br;
                                    s = checkACL(s,session,gfad.DocumentACL);
                                }
                                s+= "checking validity of document custom page *override*"+gfad.Component+br;
                                // check component override
                                if (gfad.Component == null)
                                {
                                    s+= "no document custom page override specified"+br;
                                }
                                else if ("".equals(gfad.Component) || "none".equals(gfad.Component))
                                {
                                    s+= "document has been overrided to have no custom page specified"+br;
                                }
                                else
                                {
                                    s = validateCustomComponent("newdoccontainer",gfad.Component,s);
                                    s = validateCustomComponent("mrcsimport",gfad.Component,s);
                                }
                                // plugin overrides
                                s+="checking document custom page processor plugin *override*: ";
                                if (gfad.ComponentProcessorPlugin != null)  
                                    s = checkMrcsPlugin(s,gfad.ComponentProcessorPlugin);
                                else
                                    s+= "no override specified"+br;
                                s+="checking document post-creation plugin list *override*: ";
                                if (gfad.DocumentCreationPlugins != null)
                                    s = checkMrcsPluginList(s,gfad.DocumentCreationPlugins);
                                else
                                    s+= "no overrides specified"+br;
                                
                                // allowable formats
                                s+= "checking formats and templates *overrides*"+br;
                                if (gfad.AllowableFormats == null)
                                {
                                    s+= "no overrides specified"+br;                                    
                                }
                                else
                                {
                                    for (int i = 0; i <gfad.AllowableFormats.size(); i++)
                                    {
                                        s+="Allowable format #"+i+br;
                                        MrcsDocumentAllowableFormat format = (MrcsDocumentAllowableFormat)gfad.AllowableFormats.get(i);
                                        // don't I need to further restrict the search based on the doc's DCTM systype?
                                        String dqlquery = "SELECT name,description,dos_extension FROM dm_format WHERE is_hidden=0 AND name ='"+format.Format+"'";
                                        DfQuery dfquery = new DfQuery();
                                        dfquery.setDQL(dqlquery);
                                        IDfCollection idfcollection = null;
                                        try {
                                            String systype = ((MrcsDocumentType)app.DocumentTypes.get(gfad.DocumentType)).DocumentumSystemType;
                                            idfcollection = dfquery.execute(session, 0); 
                                            if (idfcollection.next())
                                            {
                                                s+= "format "+format.Format+" is valid"+br;
                                                // audit templates for the format
                                                String tmpldql = "SELECT r_object_id, object_name FROM dm_sysobject "+
                                                                 "WHERE FOLDER('/Templates',DESCEND) "+
                                                                 "AND r_object_type='"+systype+"'"+" AND a_content_type='"+format.Format+"'";
                                                DfQuery templatequery = new DfQuery();
                                                templatequery.setDQL(tmpldql);
                                                boolean found = false;
                                                IDfCollection templates = null;
                                                try {
                                                    templates = templatequery.execute(session, 0); 
                                                    while (templates.next())
                                                    {
                                                        found = true;
                                                        
                                                        String templateid = templates.getString("r_object_id");
                                                        String templatename = templates.getString("object_name");
                                                        s+="&nbsp;&nbsp;template found: ["+templateid+"] "+templatename+br;
                                                    }
                                                    if (!found)
                                                    {
                                                        s+="&nbsp;&nbsp;NO TEMPLATES FOUND FOR FORMAT"+br;
                                                    }
                                                } catch (DfException dfexception) {
                                                    s+= "exception thrown during template search:"+dfexception+br;
                                                } finally {
                                                    try {
                                                        if (templates != null)
                                                            templates.close();
                                                    } catch (DfException dfexception1) {
                                                    }
                                                }                                        
                                            }
                                            else s+= "format "+format.Format+" NOT FOUND!"+br;
                                        } catch (DfException dfexception) {
                                            s+= "format "+format.Format+" search (Exception thrown: "+dfexception+")"+br;
                                        } finally {
                                            try {
                                                if (idfcollection != null)
                                                    idfcollection.close();
                                            } catch (DfException dfexception1) {
                                            }
                                        }
                                    }
                                }                                
                            }
                            else
                            {
                                s += "Allowable Document Type "+gfad.DocumentType+" NOT FOUND in this app's defined set of document types!"+br;
                            }
                        }
                        // nested grouping folders
                        s+= "checking nested grouping folders"+br;
                        if (gftype.GroupingFolderTypes != null)
                        {
                            for (int gfti = 0; gfti < gftype.GroupingFolderTypes.size(); gfti++)
                            {
                                if (app.GroupingFolderTypes.containsKey(gftype.GroupingFolderTypes.get(gfti)))
                                    s += "Nested grouping folder type "+gftype.GroupingFolderTypes.get(gfti)+" is valid."+br;
                                else
                                    s += "Nested grouping folder type "+gftype.GroupingFolderTypes.get(gfti)+" NOT FOUND!!!"+br;
                            }
                        }
                        else
                            s+= "No nested grouping folders specified"+br;
                    }
                    //lifecycles
                    s+="-----------------------------------------------------------------------------------"+br;
                    if (app.MrcsDocumentLifecycles == null)
                        s+= "WARNING: no application lifecycles specified in configuration"+br;
                    else
                    {
                        s+= "checking application lifecycles"+br;
                        Iterator lifecycles = app.MrcsDocumentLifecycles.keySet().iterator();
                        while (lifecycles.hasNext())
                        {
                            String lckey = (String)lifecycles.next();
                            Lifecycle cycle = (Lifecycle)app.MrcsDocumentLifecycles.get(lckey);
                            s += "checking lifecycle "+cycle.LifecycleSystemName+br;
                            IDfSysObject cycleobj = (IDfSysObject)session.getObjectByQualification("dm_sysobject where object_name ='" + cycle.LifecycleSystemName + "'");
                            if (cycleobj == null)
                                s+= "--> lifecycle NOT FOUND"+br;
                            else
                                s+= "--> lifecycle object found"+br;
                        }                        
                    }
                    
                    //WFs
                    s+="-----------------------------------------------------------------------------------"+br;
                    if (app.MrcsWFTemplates == null)
                        s+= "WARNING: no application workflow templates specified in configuration"+br;
                    else
                    {
                        s+= "checking workflow template"+br;
                        Iterator wfts = app.MrcsWFTemplates.keySet().iterator();
                        while (wfts.hasNext())
                        {
                            String lckey = (String)wfts.next();
                            MrcsWFTemplate wft = (MrcsWFTemplate)app.MrcsWFTemplates.get(lckey);
                            s += "checking workflow template "+wft.WFID+br;
                            IDfSysObject cycleobj = (IDfSysObject)session.getObjectByQualification("dm_sysobject where object_name ='" + wft.WFID + "'");
                            if (cycleobj == null)
                                s+= "--> workflow template NOT FOUND"+br;
                            else
                                s+= "--> workflow template object found"+br;
                        }                        
                    }
                    
                    sMgr.release(session);
                }
                else
                {
                    s+= "-- this application not valid for the current docbase!!!!"+br;
                }

            }
        } catch (Exception e) {
            s += "Exception occurred during audit: "+e;
        }
        
        return s;
    }


    public static ConfigFile getConfigFile(IConfigElement iconfigelement)
    {
        ConfigFile configfile;
        for(configfile = null; configfile == null && iconfigelement != null; iconfigelement = iconfigelement.getParent())
            if(iconfigelement instanceof ConfigFile)
                configfile = (ConfigFile)iconfigelement;

        return configfile;
    }
    
    public static String validateCustomComponent(String mrcscomponentid, String customcomponent, String s)
    {
        if (customcomponent == null)
        {
            return s+"no custom page specified"+br;
        }
        IConfigElement newdocconfigelements[] = ConfigService.getPrimaryElements("component[id=" + mrcscomponentid + "]");
        if (newdocconfigelements.length == 0)
        {
            return s+"ValidateCustomComponent warning: componentid "+mrcscomponentid+" not found by ConfigService"+br;
        }
        // grab the last one...
        ConfigFile newdoccfg = getConfigFile(newdocconfigelements[newdocconfigelements.length-1]);
        if (newdoccfg != null)
        {
            IConfigElement icescope = newdoccfg.getChildElement("scope");
            if (icescope != null)
            {
                IConfigElement icecomp = icescope.getChildElement("component");
                if (icecomp != null)
                {
                    IConfigElement icepages = icecomp.getChildElement("pages");
                    if (icepages != null)
                    {
                        IConfigElement thepage = icepages.getChildElement(customcomponent);
                        if (thepage != null)
                        {
                            return s+"Custom Page "+customcomponent+" was found in config file for "+mrcscomponentid+br;
                        }
                        else
                        {
                            return s+"Custom Page "+customcomponent+" was NOT FOUND in config file for "+mrcscomponentid+br;
                        }
                    }
                    else
                    {
                        return s+"ValidateCustomComponent warning: pages element not found in config file"+br;                
                    }
                }
                else
                {
                    return s+"ValidateCustomComponent warning: component element not found in config file"+br;                
                }
            }
            else
            {
                return s+"ValidateCustomComponent warning: scope element not found in config file"+br;                
            }
        }
        else
        {
            return s+"ValidateCustomComponent warning: config file for "+mrcscomponentid+" not found by ConfigService"+br;            
        }
    }

}
