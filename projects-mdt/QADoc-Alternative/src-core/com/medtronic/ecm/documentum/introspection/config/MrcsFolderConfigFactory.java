/*
 * Created on Jan 21, 2005
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

 Filename       $RCSfile: MrcsFolderConfigFactory.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:38 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.common.DfLogger;


/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsFolderConfigFactory extends GenericConfigFactory {
    
    static MrcsFolderConfigFactory singletonFolderConfig;
    
    private MrcsFolderConfigFactory()
    {
        super();
    }
    
    public static MrcsFolderConfigFactory getFolderConfig()
    {
        
        // our factory method for configbroker
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled())DfLogger.getRootLogger().debug("MRCS:FolderConfigFactory.getFolderConfig - check if we need to instantiate");
        if (singletonFolderConfig == null) {
            synchronized (MrcsFolderConfigFactory.class) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled())DfLogger.getRootLogger().debug("MRCS:FolderConfigFactory.getFolderConfig - need to instantiate and load config");
                singletonFolderConfig = new MrcsFolderConfigFactory();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled())DfLogger.getRootLogger().debug("MRCS:FolderConfigFactory.getFolderConfig - config loaded");
            }
        }
        return singletonFolderConfig;
    }

    public MrcsRootFolderMatchDTO getGroupingFolderTypesForRootFolder(String currentpath, String docbase)
    {
        // this might unfortunately be kind of expensive...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - Begin checking "+docbase+"::"+currentpath,null,null);
        // iterate through the application objects
        HashMap applicationConfig = getApplications();
        Iterator apps = applicationConfig.values().iterator();        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - Iterating through configured MrcsApplications",null,null);
        while (apps.hasNext())
        {
            MrcsApplication curApp = (MrcsApplication)apps.next();
            if (curApp.DocBase.equals(docbase))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - current App: "+curApp.ApplicationName,null,null);
                // for each application object, match the folder against the root folder patterns
            	if (curApp.RootFolders != null) 
            	{
	                for (int i=0; i < curApp.RootFolders.size(); i++)
	                {
	                    MrcsRootFolder roots = (MrcsRootFolder)curApp.RootFolders.get(i);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - docbase matched, match folder "+roots.FolderPath,null,null);
	                    if (roots.getFolderPathCompiledRE().match(currentpath))
	                    {
	                        // need a transfer object to pass application and gftypes?
	                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - docbase and folder both matched, returning GF Type list DTO",null,null);
	                        MrcsRootFolderMatchDTO matchingGFs = new MrcsRootFolderMatchDTO();
	                        matchingGFs.groupingFolderTypes = roots.GroupingFolderTypes;
	                        matchingGFs.mrcsApplication = curApp.ApplicationName;
	                        return matchingGFs;	                        
	                    }
	                }
            	}
            }
        }
        // nothing found...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForRootFolder - no matches found",null,null);
        return null;
    }

    public List getGroupingFolderTypesForGroupingFolder(String parentGroupingFolderType, String mrcsApplication)
    {
        // go through the applications looking for the grouping folder type
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForGroupingFolder - Top, look up the application: " +mrcsApplication,null,null);
        HashMap applicationConfig = getApplications();
        MrcsApplication mrcsApp = (MrcsApplication)applicationConfig.get(mrcsApplication);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForGroupingFolder - look up the grouping folder type: " +parentGroupingFolderType,null,null);
        MrcsGroupingFolderType gft = (MrcsGroupingFolderType)mrcsApp.GroupingFolderTypes.get(parentGroupingFolderType);
        List gflist = new ArrayList();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForGroupingFolder - check if this GF Type allows other GFs as subfolders",null,null);
        if (gft.GroupingFolderTypes == null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForGroupingFolder - no is doesn't, returning empty arraylist (should be null?)",null,null);
            return gflist;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderTypesForGroupingFolder - GF subfolders allowed, returning the list of valid GF types",null,null);
        for (int i=0; i < gft.GroupingFolderTypes.size(); i++)
        {
            String currentType = (String)gft.GroupingFolderTypes.get(i);
            gflist.add(currentType);
        }
        return gflist;
    }

    public String getGroupingFolderComponent(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponent - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponent - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponent - returning component "+gftype.Component,null,null);
        return gftype.Component;
    }
    
    public MrcsPlugin getGroupingFolderComponentProcessor(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponentProcessor - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponentProcessor - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponentProcessor - retrieving gfcomponentprocessor",null,null);
        if (gftype.ComponentProcessorPlugin == null || gftype.ComponentProcessorPlugin.PluginClassName == null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponentProcessor - no processor defined, will use default processor",null,null);
            return null;
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderComponentProcessor - returning component processor: "+gftype.ComponentProcessorPlugin.PluginClassName,null,null);
            return gftype.ComponentProcessorPlugin;
        }
    }

    public String getGroupingFolderACL(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderACL - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderACL - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderACL - returning GF ACL "+gftype.GroupingFolderACL,null,null);
        return gftype.GroupingFolderACL;
    }

    public String getSubfolderComponent(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponent - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponent - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponent - returning subfolder component "+gftype.SubfolderComponent,null,null);
        return gftype.SubfolderComponent;        
    }

    public MrcsPlugin getSubfolderComponentProcessor(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponentProcessor - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponentProcessor - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponentProcessor - retrieving gfcomponentprocessor",null,null);
        if (gftype.SubfolderComponentProcessorPlugin == null || gftype.SubfolderComponentProcessorPlugin.PluginClassName == null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponentProcessor - no processor defined, will use default processor",null,null);
            return null;
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderComponentProcessor - returning component processor: "+gftype.SubfolderComponentProcessorPlugin.PluginClassName,null,null);
            return gftype.SubfolderComponentProcessorPlugin;
        }
    }

    public String getSubfolderACL(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderACL - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderACL - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderACL - returning subfolderACL "+gftype.SubfolderACL,null,null);
        return gftype.SubfolderACL;
    }

    public String getGroupingFolderDocumentumType(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderDocumentumType - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderDocumentumType - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderDocumentumType - returning DCTM systype "+gftype.DocumentumSystemType,null,null);
        return gftype.DocumentumSystemType;        
    }

    public String getGroupingFolderNamingFormatPlugin(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatPlugin - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatPlugin - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatPlugin - returning plugin classname "+gftype.GroupingFolderNamingFormat.PluginClassName,null,null);
        return gftype.GroupingFolderNamingFormat.PluginClassName;        
    }

    public Map getGroupingFolderNamingFormatConfig(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatConfig - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatConfig - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderNamingFormatConfig - returning config",null,null);
        return gftype.GroupingFolderNamingFormat.PluginConfiguration;
    }
    
    public List getGroupingFolderCreationPlugins(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        if (gftype.GroupingFolderCreationPlugins != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - list of plugins found",null,null);
            ArrayList plugins = new ArrayList();
            for (int i=0; i < gftype.GroupingFolderCreationPlugins.size(); i++)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - processing plugin #"+i,null,null);
                MrcsPlugin configplugin = (MrcsPlugin)gftype.GroupingFolderCreationPlugins.get(i);
                plugins.add(configplugin);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - returning plugin list",null,null);
            return plugins;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getGroupingFolderCreationPlugins - no plugins found, returning null",null,null);
        return null;
    }

    public List getSubfolderCreationPlugins(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        if (gftype.SubfolderCreationPlugins != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - list of plugins found",null,null);
            ArrayList plugins = new ArrayList();
            for (int i=0; i < gftype.SubfolderCreationPlugins.size(); i++)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - processing plugin #"+i,null,null);
                MrcsPlugin configplugin = (MrcsPlugin)gftype.SubfolderCreationPlugins.get(i);
                plugins.add(configplugin);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - returning plugin list",null,null);
            return plugins;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderCreationPlugins - no plugins found, returning null",null,null);
        return null;
    }


    public String getSubfolderDocumentumType(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderDocumentumType - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderDocumentumType - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderDocumentumType - returning DCTM systype "+gftype.SubfolderDocumentumType,null,null);
        return gftype.SubfolderDocumentumType;        
    }

    public String getSubfolderNamingFormatPlugin(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatPlugin - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatPlugin - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatPlugin - returning plugin classname "+gftype.SubfolderNamingFormat.PluginClassName,null,null);
        return gftype.SubfolderNamingFormat.PluginClassName;        
    }

    public Map getSubfolderNamingFormatConfig(String application, String groupingfoldertype)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatConfig - top, getting app "+application,null,null);
        MrcsApplication mrcsapp = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatConfig - looking up gftype "+groupingfoldertype,null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:FolderConfigFactory.getSubfolderNamingFormatConfig - returning config",null,null);
        return gftype.SubfolderNamingFormat.PluginConfiguration;
    }
    
    public boolean getGroupingFolderSubfolderDisallowFlag(String application, String groupingfoldertype)
    {
        MrcsApplication mrcsapp = getApplication(application);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(groupingfoldertype);
        return gftype.DisallowUserCreatedSubfolders;        
    }
}
