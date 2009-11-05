/*
 * Created on Dec 28, 2004
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

 Filename       $RCSfile: MrcsConfigBroker.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.6 $
 Modified on    $Date: 2008/09/22 20:29:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.regexp.RE;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.medtronic.documentum.mrcs.util.REFactory;
import com.thoughtworks.xstream.XStream;

/**
 * @author muellc4
 *
 * Current plan for the configbroker: create a bunch of PoJos that represent the various configuration settings.
 * Serialize these PoJos to XML using the XStream, and the main config XML is stored as a versioned, managed
 * document in Documentum, so configuration changes can be fully tracked and managed.
 * - separate background thread to check if the base config doc has been updated and needs reloading?
 * - appropriate synchonizing of objects in case of config reload by background thread?
 * - use static methods instead of instantiated class? Singleton class referenced by JNDI (same thing really...)
 * - check out XStream aliases to alleviate full classpaths in deserialization
 */


public class MrcsConfigBroker {

    // static...ConfigBroker is a Factory, should be accessed by invoking:
    //    ConfigBroker config = ConfigBroker.getConfigBroker();
    private static MrcsConfigBroker singletonConfigBroker;
    private static ResourceBundle configRB;

    private static String     CONFIGDIRECTORY;
    private static String     ALIASFILE;
    private static RE         APPFILEPATTERN;

    private static String     CONFIGSTORAGEMETHOD;
    private static String     CONFIGDOCBASE;
    private static String     DOCBASEUSERNAME;
    private static String     DOCBASEPASSWORD;

    private static String     MODIFIEDTRACKER;
    private static int        SLEEPTIME = 60000; // default to 1 minute

    // static class load init code
    static {
        try {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker static initializer - setting up the configdir, etc.");
        configRB = ResourceBundle.getBundle ("com.medtronic.documentum.mrcs.config.MrcsConfigBrokerResource");
        MrcsConfigBroker.CONFIGDIRECTORY = configRB.getString("CONFIGDIRECTORY");
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker static initializer - configdir: "+CONFIGDIRECTORY);
        MrcsConfigBroker.ALIASFILE       = configRB.getString("ALIASFILE");
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker static initializer - aliasfile: "+ALIASFILE);
        MrcsConfigBroker.APPFILEPATTERN  = REFactory.createRE(configRB.getString("APPFILEPATTERN"));
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker static initializer - appfilepattern: "+configRB.getString("APPFILEPATTERN"));
        MrcsConfigBroker.CONFIGSTORAGEMETHOD  = configRB.getString("CONFIGSTORAGEMETHOD");
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker static initializer - appfilepattern: "+CONFIGSTORAGEMETHOD);
        if ("DCTM".equals(CONFIGSTORAGEMETHOD))
        {
            // config xml is stored in a DCTM docbase
            MrcsConfigBroker.CONFIGDOCBASE  = configRB.getString("CONFIGDOCBASE");
            MrcsConfigBroker.DOCBASEUSERNAME  = configRB.getString("DOCBASEUSERNAME");
            MrcsConfigBroker.DOCBASEPASSWORD  = configRB.getString("DOCBASEPASSWORD");
            MrcsConfigBroker.MODIFIEDTRACKER  = configRB.getString("MODIFIEDTRACKER");
            if (MrcsConfigBroker.MODIFIEDTRACKER != null)
            {
                MrcsConfigBroker.SLEEPTIME = Integer.parseInt(configRB.getString("SLEEPTIME"));
            }
        }
        } catch (Exception e) {
            String s = e.getMessage();
            throw new RuntimeException("Error in MRCS Config static initialization: "+s,e);
        }
    }

    public static MrcsConfigBroker getConfigBroker()
    {
        // our factory method for configbroker
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker.getConfigBroker - check if we need to instantiate");
        if (singletonConfigBroker == null) {
            synchronized (MrcsConfigBroker.class) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker.getConfigBroker - need to instantiate and load config");
                singletonConfigBroker = new MrcsConfigBroker();
                try {
                    if ("DCTM".equals(CONFIGSTORAGEMETHOD))
                    {
                        // launch monitoring thread
                        if (MODIFIEDTRACKER != null)
                        {
                            configmonitor = new Timer();
                            // this is really funky -> you can't instantiate a inner class inside of a static method
                            // unless you have an instance of the parent class already...
                            configmonitor.schedule(singletonConfigBroker.new ConfigMonitorTask(),SLEEPTIME, SLEEPTIME);
                        }
                        // decode xmls
                        singletonConfigBroker.loadConfigFromDCTM();
                    } else {
                        singletonConfigBroker.loadConfig();
                    }
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsConfigBroker.getConfigBroker - config loaded");
                } catch (Exception e) {
                    singletonConfigBroker = null;
                    throw new RuntimeException(e);
                }
            }
        }
        return singletonConfigBroker;
    }

    public HashMap getApplications()
    {
        return applications;
    }

    // object methods and properties... (non-static)
    HashMap applications = new HashMap();

    public void loadConfig() throws Exception
    {
        applications = new HashMap();
        // Step 1. load aliases from config directory...

        // -- set xstream deserializer and alias the ConfigAlias class (a little inner class to structure the data xml-wise)
        class ConfigAlias {String Alias; String Class;} // inner class definition
        XStream xstream = new XStream();
        xstream.alias("ConfigAlias", ConfigAlias.class);
        // -- deserialize from the file configbroker.xml in the CONFIGDIRECTORY
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - opening aliasfile "+CONFIGDIRECTORY+ALIASFILE,null,null);
        BufferedReader aliasfilereader;
        try {
            aliasfilereader = new BufferedReader(new FileReader(CONFIGDIRECTORY + ALIASFILE));
        } catch (IOException ioe) {
            /*-ERROR-*/DfLogger.error(null,null,null,ioe);
            throw new Exception("Mrcs could not load alias listing (configbroker.xml) due to file errors.",ioe);
        }
        // -- cast the aliases as a List of ConfigAlias objects, close file
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - file opened successfully, deserializing the List of Strings",null,null);
        List aliases = (List)xstream.fromXML(aliasfilereader);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - list deserialized, closing the alias config file",null,null);
        aliasfilereader.close();

        // Step 2. load application configuration files

        // -- prep the MrcsApplication configuration files XStream deserializer, setting the aliases we just read from configbroker.xml
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - create new XStream",null,null);
        xstream = new XStream();
        for (int i=0; i < aliases.size(); i++)
        {
            ConfigAlias currentalias = (ConfigAlias)aliases.get(i);
            xstream.alias(currentalias.Alias,Class.forName(currentalias.Class));
        }
        // -- get a list of mrcs app config files (defines a simple inner class to do the file filtering)
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - getting list of app config xml files from "+CONFIGDIRECTORY,null,null);
        File configdir = new File(CONFIGDIRECTORY);
        class AppCfgFilenameFilter implements FilenameFilter // inner class def for our mrcsapp_*.xml file filter
        {
            public boolean accept(File dir, String name) { return APPFILEPATTERN.match(name); }
        }
        File[] filelist = configdir.listFiles(new AppCfgFilenameFilter());
        // -- check if the file list is empty, which is bad
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - check if we found at least one appfile: " + filelist.length,null,null);
        if (filelist.length < 1)
        {
            Exception e = new Exception("No MRCS application configuration files found (should be named mrcsapp_*.xml) in "+CONFIGDIRECTORY);
            /*-ERROR-*/DfLogger.error(null,null,null,e);
            throw e;
        }
        // -- iterate through the app config xml files, deserialize, add to app store
        BufferedReader appfilerdr;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - iterating through file list",null,null);
        for (int j=0; j < filelist.length; j++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - opening "+filelist[j].getName(),null,null);
            try { appfilerdr = new BufferedReader(new FileReader(CONFIGDIRECTORY + filelist[j].getName()));
            } catch (IOException ioe) {
                Exception e = new Exception("File error reading MRCS Application config file "+filelist[j].getName(),ioe);
                /*-ERROR-*/DfLogger.error(null,null,null,e);
                throw e;
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - file opened, attempting MrcsApplication deserialization",null,null);
            MrcsApplication mrcsapp = (MrcsApplication)xstream.fromXML(appfilerdr);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - deserialization successful for MrcsApplication: "+mrcsapp.ApplicationName,null,null);
            this.applications.put(mrcsapp.ApplicationName,mrcsapp);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - closing current file",null,null);
            appfilerdr.close();
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - Config Broker initialization complete",null,null);

    }

    public void loadConfigFromDCTM() throws Exception
    {
        applications = new HashMap();

        // connect to documentum...
        //create Client objects
        IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();

        //create a Session Manager object
        IDfSessionManager sMgr = client.newSessionManager();

        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(DOCBASEUSERNAME);
        loginInfoObj.setPassword(DOCBASEPASSWORD);
        loginInfoObj.setDomain(null);
        sMgr.setIdentity(CONFIGDOCBASE, loginInfoObj);

        // get a session...
        IDfSession session = null;
        
        try {
        	session = sMgr.getSession(CONFIGDOCBASE);

	        // Step 1. load aliases from config directory...
	        // -- set xstream deserializer and alias the ConfigAlias class (a little inner class to structure the data xml-wise)
	        class ConfigAlias {String Alias; String Class;} // inner class definition
	        XStream xstream = new XStream();
	        xstream.alias("ConfigAlias", ConfigAlias.class);
	        // -- deserialize from the file configbroker.xml in the CONFIGDIRECTORY
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - locating aliasfile "+CONFIGDIRECTORY+ALIASFILE,null,null);
	        String aliasfilename = CONFIGDIRECTORY+ALIASFILE;
	        IDfSysObject sysObj = (IDfSysObject)session.getObjectByPath(aliasfilename);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - attempting to load into memory, sysobject successfully found? "+(sysObj!=null),null,null);
	        ByteArrayInputStream bais = sysObj.getContent();
	        String aliasfilecontent = null;
	        if (bais.available() > 0)
	        {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - unmarshalling ByteArrayInputStream",null,null);
	            aliasfilecontent = clientx.ByteArrayInputStreamToString(bais);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - unmarshalled aliasfilecontent: "+aliasfilecontent,null,null);
	        } else {
	            throw new Exception("Mrcs ConfigBroker unable to load aliasfile "+CONFIGDIRECTORY+ALIASFILE+" from docbase "+CONFIGDOCBASE);
	        }
	        // -- cast the aliases as a List of ConfigAlias objects, close file
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - file opened successfully, deserializing the List of aliases",null,null);
	        List aliases = (List)xstream.fromXML(aliasfilecontent);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - list deserialized",null,null);
	
	        // Step 2. load application configuration files
	
	        // -- prep the MrcsApplication configuration files XStream deserializer, setting the aliases we just read from configbroker.xml
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - create new XStream",null,null);
	        xstream = new XStream();
	        for (int i=0; i < aliases.size(); i++)
	        {
	            ConfigAlias currentalias = (ConfigAlias)aliases.get(i);
	            xstream.alias(currentalias.Alias,Class.forName(currentalias.Class));
	        }
	        // -- get a list of mrcs app config files (defines a simple inner class to do the file filtering)
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - getting list of app config xml files from "+CONFIGDIRECTORY,null,null);
	        IDfFolder configdir = (IDfFolder)session.getObjectByPath(CONFIGDIRECTORY.substring(0,CONFIGDIRECTORY.length()-1));
	        IDfCollection filelist = configdir.getContents("object_name");
	        boolean filesfound = false;
	        while (filelist.next())
	        {
	            IDfTypedObject to = filelist.getTypedObject();
	            String objname = to.getString("object_name");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - matching file "+objname+" against appfile pattern",null,null);
	            if (APPFILEPATTERN.match(objname))
	            {
	                String configfilename = CONFIGDIRECTORY+objname;
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - match successful, looking up "+configfilename+" in the docbase",null,null);
	                IDfSysObject configfileObj = (IDfSysObject)session.getObjectByPath(configfilename);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - attempting to get content (was file found? "+(configfileObj!=null)+")",null,null);
	                bais = configfileObj.getContent();
	                if (bais.available() > 0)
	                {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - unmarshalling ByteArrayInputStream",null,null);
	                    String configfilecontent = clientx.ByteArrayInputStreamToString(bais);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - unmarshalled configfilecontnet: "+configfilecontent,null,null);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - file opened, attempting MrcsApplication deserialization",null,null);
	                    MrcsApplication mrcsapp = (MrcsApplication)xstream.fromXML(configfilecontent);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - deserialization successful for MrcsApplication: "+mrcsapp.ApplicationName,null,null);
	                    this.applications.put(mrcsapp.ApplicationName,mrcsapp);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - closing current file",null,null);
	                } else {
	                    /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsConfigBroker.loadConfig - error loading configfile "+configfilename+" - bais not available",null,null);
	                    throw new Exception("Mrcs ConfigBroker unable to load configfile "+configfilename+" from docbase "+CONFIGDOCBASE);
	                }
	            }
	        }
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.loadConfig - closing file list collection",null,null);
	        if (filelist != null)
	            filelist.close();
	
	        if (MODIFIEDTRACKER != null)
	        {
	            String modifiedtrackerfilename = CONFIGDIRECTORY+MODIFIEDTRACKER;
	            IDfSysObject tracker = (IDfSysObject)session.getObjectByPath(modifiedtrackerfilename);
	            lastModified = tracker.getModifyDate(); // will this get to the tracker?
	        }
        } finally {
        	try { sMgr.release(session); } catch (Exception e) {}
        }
	
    }

    // config monitor thread
    static Timer configmonitor = null;
    static IDfSessionManager configmonitormanager = null;
    static IDfTime lastModified;
    class ConfigMonitorTask extends TimerTask
    {
        public void run()
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - top",null,null);
            try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - start next scan",null,null);
                // poll DCTM intermittently to see if config has changed...
                // connect to documentum...
                //create Client objects
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - checking monitormanager",null,null);
                if (configmonitormanager == null)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - instantiating monitormanager",null,null);
                    IDfClientX clientx = new DfClientX();
                    IDfClient client = clientx.getLocalClient();

                    //create a Session Manager object
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - creating SessionManager",null,null);
                    configmonitormanager = client.newSessionManager();

                    //create an IDfLoginInfo object named loginInfoObj
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - setting login credentials",null,null);
                    IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
                    loginInfoObj.setUser(DOCBASEUSERNAME);
                    loginInfoObj.setPassword(DOCBASEPASSWORD);
                    loginInfoObj.setDomain(null);
                    configmonitormanager.setIdentity(CONFIGDOCBASE, loginInfoObj);
                }

                // get a session...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - check modified tracker file",null,null);
                IDfSession session = null;
                try {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - get session",null,null);
                    session = configmonitormanager.getSession(CONFIGDOCBASE);

                    String modifiedtrackerfilename = CONFIGDIRECTORY+MODIFIEDTRACKER;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - checking "+modifiedtrackerfilename,null,null);
                    IDfSysObject sysObj = (IDfSysObject)session.getObjectByPath(modifiedtrackerfilename);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - found? "+(sysObj!=null),null,null);
                    boolean b = (sysObj.getModifyDate().compareTo(lastModified) > 0);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - newer? "+b,null,null);
                    if (b)
                    {
                        synchronized (singletonConfigBroker)
                        {
                            try {
                                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - triggering reload!",null,null);
                                singletonConfigBroker.loadConfigFromDCTM();
                                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsConfigBroker.run() - reload completed",null,null);
                            }catch (Exception e) {
                                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsConfigBroker.run() - config reload failed",e);
                            }

                        }
                    }

                } catch (Exception e) {
                    /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsConfigBroker.run() - error in modified tracker scan",e);
                } finally {
                    if (session != null)
                    {
                        configmonitormanager.release(session);
                    }
                }
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsConfigBroker.run (monitor thread) - exception in conig monitor thread",e);
            }
        }
    }
    
	public XStream getPreppedXStreamFromDCTM() throws Exception
	{
		/*-CONFIG-*/String m="getPreppedXStream - ";
        // connect to documentum...
        //create Client objects
        IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();

        //create a Session Manager object
        IDfSessionManager sMgr = client.newSessionManager();

        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(DOCBASEUSERNAME);
        loginInfoObj.setPassword(DOCBASEPASSWORD);
        loginInfoObj.setDomain(null);
        sMgr.setIdentity(CONFIGDOCBASE, loginInfoObj);

        // get a session...
        IDfSession session = sMgr.getSession(CONFIGDOCBASE);

        // Step 1. load aliases from config directory...
        // -- set xstream deserializer and alias the ConfigAlias class (a little inner class to structure the data xml-wise)
        class ConfigAlias {String Alias; String Class;} // inner class definition
        XStream xstream = new XStream();
        xstream.alias("ConfigAlias", ConfigAlias.class);
        // -- deserialize from the file configbroker.xml in the CONFIGDIRECTORY
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"locating aliasfile "+CONFIGDIRECTORY+ALIASFILE,null,null);
        String aliasfilename = CONFIGDIRECTORY+ALIASFILE;
        IDfSysObject sysObj = (IDfSysObject)session.getObjectByPath(aliasfilename);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attempting to load into memory, sysobject successfully found? "+(sysObj!=null),null,null);
        ByteArrayInputStream bais = sysObj.getContent();
        String aliasfilecontent = null;
        if (bais.available() > 0)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"unmarshalling ByteArrayInputStream",null,null);
            aliasfilecontent = clientx.ByteArrayInputStreamToString(bais);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"unmarshalled aliasfilecontent: "+aliasfilecontent,null,null);
        } else {
            throw new Exception("Mrcs ConfigBroker unable to load aliasfile "+CONFIGDIRECTORY+ALIASFILE+" from docbase "+CONFIGDOCBASE);
        }
        bais.close();
        // -- cast the aliases as a List of ConfigAlias objects, close file
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"file opened successfully, deserializing the List of aliases",null,null);
        List aliases = (List)xstream.fromXML(aliasfilecontent);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"list deserialized",null,null);
        // -- prep the MrcsApplication configuration files XStream deserializer, setting the aliases we just read from configbroker.xml
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"create new XStream",null,null);
        xstream = new XStream();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding aliases",null,null);
        for (int i=0; i < aliases.size(); i++)
        {
            ConfigAlias currentalias = (ConfigAlias)aliases.get(i);
            xstream.alias(currentalias.Alias,Class.forName(currentalias.Class));
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"aliases added, returning prepped XStream",null,null);
        
        // prepped xstream with class aliases loaded...
        return xstream;
	}
	
	public void addMrcsApplication(MrcsApplication mrcsapp)
	{
		applications.put(mrcsapp.ApplicationName,mrcsapp);
	}
	
	public IDfSessionManager getConfigSession() throws Exception
	{
        // connect to documentum...
        //create Client objects
		/*-CONFIG-*/String m = "getConfigSession() - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"create DFC client objects",null,null);
        IDfClientX clientx = new DfClientX();
        IDfClient client = clientx.getLocalClient();

        //create a Session Manager object
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"create sMgr",null,null);
        IDfSessionManager sMgr = client.newSessionManager();

        //create an IDfLoginInfo object named loginInfoObj
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get session with config lookup user...",null,null);
        IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
        loginInfoObj.setUser(DOCBASEUSERNAME);
        loginInfoObj.setPassword(DOCBASEPASSWORD);
        loginInfoObj.setDomain(null);
        sMgr.setIdentity(CONFIGDOCBASE, loginInfoObj);

        // get a session...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting session",null,null);
        return sMgr;
	}

	public String getConfigDocbase()
	{
		return CONFIGDOCBASE;
	}

    public void finalize()
    {
        //if (configmonitor != null)
        //{
        //    configmonitor.stop();
        //}
    }
}