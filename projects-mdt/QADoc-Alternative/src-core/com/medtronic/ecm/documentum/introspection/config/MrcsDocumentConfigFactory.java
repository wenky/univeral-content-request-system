/*
 * Created on Feb 9, 2005
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

 Filename       $RCSfile: MrcsDocumentConfigFactory.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:38 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsDocumentConfigFactory extends GenericConfigFactory
{
    static MrcsDocumentConfigFactory singletonFolderConfig;

    private MrcsDocumentConfigFactory()
    {
        super();
    }

    public static MrcsDocumentConfigFactory getDocumentConfig()
    {

        // our factory method for configbroker
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentConfigFactory.getDocumentConfig - check if we need to instantiate");
        if (singletonFolderConfig == null) {
            synchronized (MrcsFolderConfigFactory.class) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentConfigFactory.getDocumentConfig - need to instantiate and load config");
                singletonFolderConfig = new MrcsDocumentConfigFactory();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentConfigFactory.getDocumentConfig - config loaded");
            }
        }
        return singletonFolderConfig;
    }

    public List getAllowableDocumentTypes(String mrcsapp, String gftype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        List doctypes = gft.AllowableDocumentTypes;
        List doctypelist = new ArrayList();
        // doublecheck if doctypes specified...shouldn't happen, but...
        if (doctypes == null)
        {
            // do we allow ALL document types if nothing is specified here?
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - iterating through doctype list",null,null);
        for (int i=0; i < doctypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - processing doctype #"+i,null,null);
            MrcsGroupingFolderAllowableDocument doctype = (MrcsGroupingFolderAllowableDocument)doctypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getAllowableDocumentTypes - adding doctype "+doctype.DocumentType,null,null);
            doctypelist.add(doctype.DocumentType);
        }
        return doctypelist;
    }

    public String getDocumentComponent(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        // first get the main doctype component
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - getting global doctype "+doctype,null,null);
        MrcsDocumentType mdt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        String component = mdt.Component;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - globally defined component: "+mdt.Component,null,null);
        // now see if there is an override in the gf def
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentComponent - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - processing doctype #"+i,null,null);
            MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - component: "+madt.Component,null,null);
            if ("".equals(madt.Component) || "none".equals(madt.Component))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - override specified no component",null,null);
                return null;
            }
            if (madt.Component != null)
            {
                component = madt.Component; // overriding...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - returning locally overridden component",null,null);
                return component;
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponent - returning globally defined component",null,null);
        return component;
    }

    public String getGroupingFolderDocumentNamingFormatPlugin(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatPlugin - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatPlugin - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatPlugin - doc naming plugin: "+gft.DocumentNamingFormat.PluginClassName,null,null);
        return gft.DocumentNamingFormat.PluginClassName;
    }

    public Map getGroupingFolderDocumentNamingFormatConfig(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatConfig - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatConfig - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getGroupingFolderDocumentNamingFormatConfig - returning doc naming config",null,null);
        return gft.DocumentNamingFormat.PluginConfiguration;
    }

    public List getDocumentFormats(String mrcsapp, String gftype, String doctype)
    {
        // -- return null if there is none in config, or if there are too many in config. either one-and-only-one format is found, or return null
        List formats = new ArrayList();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);

        // check for overrides for this doctype...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        List overrides = gft.AllowableDocumentTypes;
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentFormats - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - looking for match for doctype "+doctype,null,null);
        for (int i=0; i < overrides.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - check #"+i,null,null);
            MrcsGroupingFolderAllowableDocument gfallowdt = (MrcsGroupingFolderAllowableDocument)overrides.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - checking "+gfallowdt.DocumentType,null,null);
            if (gfallowdt.DocumentType.equals(doctype))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - matched, checking if format override specified",null,null);
                // found an override specification...
                if (gfallowdt.AllowableFormats != null)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - override found",null,null);
                    for (int j=0; j < gfallowdt.AllowableFormats.size(); j++)
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - processing #"+j,null,null);
                        MrcsDocumentAllowableFormat docformat = (MrcsDocumentAllowableFormat)gfallowdt.AllowableFormats.get(j);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - adding "+docformat.Format,null,null);
                        formats.add(docformat.Format);
                    }
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - returning override format list",null,null);
                    return formats;
                }
            }
        }

        // if we're at this point, there were no overrides detected, so we lookup and return the globally defined doctype's formats...
        // look up the default format from the global document type config
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - looking up global doctype definition",null,null);
        MrcsDocumentType dt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        for (int k=0; k < dt.AllowableFormats.size(); k++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - processing record "+k,null,null);
            MrcsDocumentAllowableFormat docformat = (MrcsDocumentAllowableFormat)dt.AllowableFormats.get(k);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - adding format "+docformat.Format,null,null);
            formats.add(docformat.Format);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentFormats - returning globally defined format list",null,null);
        return formats;
        // that was a doozy, overrides do that...
    }

    // mueller...is there a good reason for this method existing?
    public boolean validateDocumentFormat(String mrcsapp, String gftype, String doctype, String format)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.validateDocumentFormat - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);

        //check the gf for overrides...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.validateDocumentFormat - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        List overrides = gft.AllowableDocumentTypes;
        for (int i=0; i < overrides.size(); i++)
        {
            MrcsGroupingFolderAllowableDocument gfallowdt = (MrcsGroupingFolderAllowableDocument)overrides.get(i);
            if (gfallowdt.DocumentType.equals(doctype))
            {
                List formats = gfallowdt.AllowableFormats;
                for (int j=0; j < formats.size(); j++)
                {
                    MrcsDocumentAllowableFormat allowformat = (MrcsDocumentAllowableFormat)formats.get(j);
                    if (allowformat.Format.equals(format))
                    {
                        return true;
                    }
                }
                // if not found, then return false, since the override record was present, but the format was now in the allowable list
                return false;
            }
        }

        // no override was specified for this doctype in the gf folder specification, so we check the global doctype...
        MrcsDocumentType dt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        for (int k=0; k < dt.AllowableFormats.size(); k++)
        {
            MrcsDocumentAllowableFormat allowformat = (MrcsDocumentAllowableFormat)dt.AllowableFormats.get(k);
            if (allowformat.Format.equals(format))
            {
                return true;
            }
        }
        // no matches found whatsoever...
        return false;
    }
    
    public List getDocumentFormatTemplates(String mrcsapp, String gftype, String doctype, String format)
    {
        /*-CONFIG-*/String m="MRCS:getDocumentFormatTemplates - ";
        MrcsApplication app = getApplication(mrcsapp);

        // check for overrides for this doctype...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        List overrides = gft.AllowableDocumentTypes;
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"no allowable doctypes specified for gf");
            throw new NullPointerException("MRCS Configuration Error: Template retrieval: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking for match for doctype "+doctype,null,null);
        for (int i=0; i < overrides.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check #"+i,null,null);
            MrcsGroupingFolderAllowableDocument gfallowdt = (MrcsGroupingFolderAllowableDocument)overrides.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking "+gfallowdt.DocumentType,null,null);
            if (gfallowdt.DocumentType.equals(doctype))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"matched, checking if format override specified",null,null);
                // found an override specification...
                if (gfallowdt.AllowableFormats != null)
                {
                    // search formats for matching format
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"override found",null,null);
                    for (int j=0; j < gfallowdt.AllowableFormats.size(); j++)
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing #"+j,null,null);
                        MrcsDocumentAllowableFormat docformat = (MrcsDocumentAllowableFormat)gfallowdt.AllowableFormats.get(j);
                        if (format.equals(docformat.Format))
                        {
                            // return templates
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning allowable template list",null,null);
                            return docformat.AllowableTemplates;
                        }                        
                    }
                }
            }
        }

        // if we're at this point, there were no overrides detected, so we lookup and return the globally defined doctype's formats' templates...
        // look up the default format from the global document type config
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up global doctype definition",null,null);
        MrcsDocumentType dt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        for (int k=0; k < dt.AllowableFormats.size(); k++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing record "+k,null,null);
            MrcsDocumentAllowableFormat docformat = (MrcsDocumentAllowableFormat)dt.AllowableFormats.get(k);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking format "+docformat.Format,null,null);
            if (format.equals(docformat.Format)){
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format matched, returning allowable template list",null,null);
                return docformat.AllowableTemplates;
            }
        }
        /*-ERROR-*/DfLogger.error(this,m+"if we got here, then throw NPE, bad search terms...",null,null);
        throw new NullPointerException("MRCS Document Config: Allowable Template lookup: invalid search terms: mrcsapp["+mrcsapp+"] gftype["+gftype+"] doctype["+doctype+"] format["+format+"]");
        // that was a doozy, overrides do that...        
    }

    public List getDocumentCreationPlugins(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        // first get the main doctype plugin (performancewise we should do this after the override lookup, not here)
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - looking up doctype "+doctype,null,null);
        MrcsDocumentType mdt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        List pluginlist = null;
        if (mdt.DocumentCreationPlugins != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - creating list of create event plugins",null,null);
            pluginlist = new ArrayList();
            for (int i=0; i < mdt.DocumentCreationPlugins.size(); i++)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - processing plugin #"+i,null,null);
                MrcsPlugin configplugin = (MrcsPlugin)mdt.DocumentCreationPlugins.get(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - classname: "+configplugin.PluginClassName,null,null);
                pluginlist.add(configplugin);
            }
        }
        // now see if there is an override in the gf def
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - scanning for matching doctype",null,null);
            MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
            if (madt.DocumentType.equals(doctype))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - match found, checking for override",null,null);
                if (madt.DocumentCreationPlugins != null)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - override list present, processing...",null,null);
                    pluginlist = new ArrayList();
                    for (int j=0; j < madt.DocumentCreationPlugins.size(); j++)
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - override #"+j,null,null);
                        MrcsPlugin configplugin = (MrcsPlugin)madt.DocumentCreationPlugins.get(j);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - pluginclass: "+configplugin.PluginClassName,null,null);
                        pluginlist.add(configplugin);
                    }
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - returning override list",null,null);
                    return pluginlist;
                }
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentCreationPlugins - returning globally defined list",null,null);
        return pluginlist;
    }

    public String getDocumentSystemType(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        // first get the main doctype plugin - does this have overrides - probably not, they should just define another doctype...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - looking up doctype "+doctype,null,null);
        MrcsDocumentType mdt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        String systype = mdt.DocumentumSystemType;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - returning systype "+systype,null,null);
        return systype;
    }

    public String getDocumentACL(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentACL - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentACL - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - doc ACL is "+gft.DocumentACL,null,null);
        if (gft.AllowableDocumentTypes != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - checking for doctype overrides",null,null);        	
        	for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        	{
        		MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
        		if (madt.DocumentType.equals(doctype))
        		{
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - doctype found in allowable documents for GF",null,null);        			
        			if (madt.DocumentACL != null && !"".equals(madt.DocumentACL))
        			{
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentSystemType - doctype-specific ACL override found: "+madt.DocumentACL,null,null);        			
        				return madt.DocumentACL;
        			}
        		}
        	}
        }
        return gft.DocumentACL;
    }

    public MrcsPlugin getDocumentComponentProcessor(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        // first get the main doctype component
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - getting global doctype "+doctype,null,null);
        MrcsDocumentType mdt = (MrcsDocumentType)app.DocumentTypes.get(doctype);
        MrcsPlugin componentplugin = null;
        if (mdt.ComponentProcessorPlugin != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - globally defined component processor class: "+mdt.ComponentProcessorPlugin.PluginClassName,null,null);
            componentplugin = mdt.ComponentProcessorPlugin;
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - globally defined component processor: "+mdt.ComponentProcessorPlugin,null,null);
        // now see if there is an override in the gf def
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - getting gf type "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentComponent - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - processing doctype #"+i,null,null);
            MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - component registered? "+(madt.Component!=null),null,null);
            if (madt.ComponentProcessorPlugin != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - returning locally overridden component processor: "+madt.ComponentProcessorPlugin.PluginClassName,null,null);
                return madt.ComponentProcessorPlugin;
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentComponentProcessor - returning globally defined component processor",null,null);
        return componentplugin;
    }

    public String getDocumentMrcsLifecycleName(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - getting gftype "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - processing doctype #"+i,null,null);
            MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - component registered? "+(madt.Component!=null),null,null);
            if (doctype.equals(madt.DocumentType))
            {
                return madt.Lifecycle;
            }
        }
        // if we get here, the doctype wasn't found, which is a no-no
        /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getMrcsDocumentLifecycleName - Doctype "+doctype+" not found for gftype "+gftype+" in mrcs app "+mrcsapp);
        throw new NullPointerException("MRCS Configuration Error: Doctype "+doctype+" not found in grouping folder "+gftype+" list of allowable document types");
    }

    public String getDocumentLifecycle(String mrcsapp, String gftype, String doctype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - getting gftype "+gftype,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gftype);
        if (gft.AllowableDocumentTypes == null)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - no allowable doctypes specified");
            throw new NullPointerException("MRCS Configuration Error: No allowable doctypes specified for gftype "+gftype+" in mrcs app "+mrcsapp);
        }
        for (int i=0; i < gft.AllowableDocumentTypes.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - processing doctype #"+i,null,null);
            MrcsGroupingFolderAllowableDocument madt = (MrcsGroupingFolderAllowableDocument)gft.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - component registered? "+(madt.Component!=null),null,null);
            if (doctype.equals(madt.DocumentType))
            {
            	// need to process compatibility here with new MrcsLifecycles map
            	if (app.MrcsLifecycles != null)
            	{
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - New server-based MrcsLifecycle detected...",null,null);
            		MrcsLifecycle lc = (MrcsLifecycle)app.MrcsLifecycles.get(madt.Lifecycle);
            		return lc.SystemName;
            	} else {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - Using legacy 4.1.2 lifecycle implementation",null,null);
					Lifecycle lc = (Lifecycle)app.MrcsDocumentLifecycles.get(madt.Lifecycle);
	                return lc.LifecycleSystemName;
            	}
            }
        }
        // if we get here, the doctype wasn't found, which is a no-no
        /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentConfigFactory.getDocumentLifecycle - Doctype "+doctype+" not found for gftype "+gftype+" in mrcs app "+mrcsapp);
        throw new NullPointerException("MRCS Configuration Error: Doctype "+doctype+" not found in grouping folder "+gftype+" list of allowable document types");
    }

    /**
     * @param mrcsapp
     * @param wfname
     * @return
     */
    
    public List getDocumentWorkflowValidationPlugins(String mrcsapp, String wfname)
    {
        /*-CONFIG-*/String m="MRCS:MrcsDocumentConfigFactory.getDocumentWorkflowValidationPlugins - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting app "+mrcsapp,null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting wf "+mrcsapp,null,null);
        if (isLegacyLCWF(mrcsapp))
        {
	        MrcsWFTemplate wft = (MrcsWFTemplate)app.MrcsWFTemplates.get(wfname);
	        if (wft.Validations != null)
	        {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"validation list present, processing...",null,null);
	            List pluginlist = new ArrayList();
	            for (int j=0; j < wft.Validations.size(); j++)
	            {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"plugin #"+j,null,null);
	                MrcsPlugin configplugin = (MrcsPlugin)wft.Validations.get(j);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"pluginclass: "+configplugin.PluginClassName,null,null);
	                pluginlist.add(configplugin);
	            }
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning override list",null,null);
	            return pluginlist;
	        }
        } else {
        	MrcsWorkflow mrcswf = (MrcsWorkflow)app.MrcsWorkflows.get(wfname);
        	return mrcswf.Validations;
        }
        return null;
    }
    
}
