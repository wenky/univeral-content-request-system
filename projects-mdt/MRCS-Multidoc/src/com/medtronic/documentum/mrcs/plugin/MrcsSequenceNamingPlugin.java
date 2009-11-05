/*
 * Created on Mar 24, 2005
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

 Filename       $RCSfile: MrcsSequenceNamingPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:57 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.MrcsFolderConfigFactory;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsSequenceNamingPlugin implements MrcsNamingFormatPlugin, MrcsDocumentNamingFormatPlugin {
    Random r = new Random(new Date().getTime());

    // MrcsNamingFormatPlugin (folder naming plugin method)
    public String generateName(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - top of plugin",null,null);
        String sequencedocname = (String)configdata.get("SequenceDocName");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - seq doc name: "+sequencedocname,null,null);
        String sequenceproperty = (String)configdata.get("SequenceProperty");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - seq doc prop: "+sequenceproperty,null,null);
        int seqnbr = this.getNextSequenceNumber(session,sequencedocname,sequenceproperty,mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - seq number: "+seqnbr,null,null);
        String name = postprocess(seqnbr, configdata);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - name post-process: "+name,null,null);
        String sequenceprefixkey = (String)configdata.get("CustomPrefixKey");
        String sequencesuffixkey = (String)configdata.get("CustomSuffixKey");
        if (sequenceprefixkey != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - custom prefix enabled",null,null);
            String prefix = (String)customdata.get(sequenceprefixkey);
            if (prefix != null)
            {
                name = prefix+name;
            }
        }
        if (sequencesuffixkey != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - custom suffix enabled",null,null);
            String suffix = (String)customdata.get(sequencesuffixkey);
            if (suffix != null)
            {
                name = name+suffix;
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(folder) - final name: "+name,null,null);
        return name;
    }
    
    // MrcsDocumentNamingFormatPlugin method
    public String generateName(IDfSession session, String mrcsapp, String gftype, String doctype, String objectid, Map configdata, Map customdata) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - top of plugin",null,null);
        String sequencedocname = (String)configdata.get("SequenceDocName");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - seq doc name: "+sequencedocname,null,null);
        String sequenceproperty = (String)configdata.get("SequenceProperty");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - seq doc prop: "+sequenceproperty,null,null);
        int seqnbr = this.getNextSequenceNumber(session,sequencedocname,sequenceproperty,mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - seq number: "+seqnbr,null,null);
        String name = postprocess(seqnbr, configdata);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - name post-process: "+name,null,null);
        String sequenceprefixkey = (String)configdata.get("CustomPrefixKey");
        String sequencesuffixkey = (String)configdata.get("CustomSuffixKey");
        if (sequenceprefixkey != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - custom prefix enabled",null,null);
            String prefix = (String)customdata.get(sequenceprefixkey);
            if (prefix != null)
            {
                name = prefix+name;
            }
        }
        if (sequencesuffixkey != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - custom suffix enabled",null,null);
            String suffix = (String)customdata.get(sequencesuffixkey);
            if (suffix != null)
            {
                name = name+suffix;
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.generateName(doc) - final name: "+name,null,null);
        return name;
    }
    
    public String postprocess(int seqnbr, Map configdata)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.postprocess - looking for registered format pattern",null,null);
        String format = (String)configdata.get("Format"); // see DecimalFormat class in J2SE api docs for pattern examples
        if (format != null)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.postprocess - pattern: "+format,null,null);
            DecimalFormat formatter = new DecimalFormat(format);
            String output = formatter.format(seqnbr);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.postprocess - output of formatter: "+output,null,null);
            return output;
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.postprocess - no formatting pattern found",null,null);
            return ""+seqnbr;
        }
    }

    public int getNextSequenceNumber(IDfSession session, String sequencedocname, String sequenceproperty, String mrcsapp) throws Exception
    {
        //return(getNextSequenceNumberOriginal(session,sequencedocname,sequenceproperty));
        //return(getNextSequenceNumberExceptionLoop(session,sequencedocname,sequenceproperty));
        // spawn a new DfClient and sessionmanager so threads aren't stepping on each others' toes with transactions
        /*-CFG-*/String m="getNextSequenceNumber-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS:create new session manager",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS:app: "+mrcsapp,null,null);
        MrcsFolderConfigFactory folderconfig = MrcsFolderConfigFactory.getFolderConfig();
        String docbase = folderconfig.getApplicationDocbase(mrcsapp);
        String user = folderconfig.getSystemUsername(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS:usr: "+user,null,null);
        String pass = folderconfig.getSystemPassword(mrcsapp);
        IDfClient client = new DfClient();          
        IDfSessionManager sMgr = client.newSessionManager();        
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(user);
        loginInfoObj.setPassword(pass);
        loginInfoObj.setDomain(null);        
        sMgr.setIdentity(docbase, loginInfoObj);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS:session manager created",null,null);        
        return(getNextSequenceNumberLockWithExceptionLoop(session,sequencedocname,sequenceproperty,sMgr));
    }
    
    
    public int getNextSequenceNumberOriginal(IDfSession session, String sequencedocname, String sequenceproperty) throws Exception
    {
        // get the sequence document
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - getting sequencedoc: "+sequencedocname,null,null);
        IDfSysObject sequencedoc = (IDfSysObject)session.getObjectByPath(sequencedocname);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checking it has sequence prop: "+sequenceproperty,null,null);
        if (!sequencedoc.hasAttr(sequenceproperty))
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence property not found on sequence doc");
            throw new Exception("MrcsSequenceNamingPlugin - sequence property "+sequenceproperty+" not found on sequence tracking document: "+sequencedocname);
        }
        // try to do a checkoutlock:
        int tries = 0;
        boolean performed = false;
        int currentsequencevalue = -1;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - attempting sequence document access",null,null);
        while (tries < 10)
        {
            tries++;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checkout try #"+tries,null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
            // start transaction via DMCL API calls
            String sessid = session.getSessionId();
            boolean bTransactionStatus = session.apiExec("begintran",sessid);
            String lockargs = session.getSessionId()+","+sequencedoc.getObjectId().getId();
            boolean bLockStatus = session.apiExec("lock",lockargs);
            if (bLockStatus) //if (!sequencedoc.isCheckedOut()) 
            {
                // lock and perform sequence computation...this isn't perfect, but it's as close as we can get...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - lock acquired!",null,null);
                //sequencedoc.checkout();
                if (sequencedoc.hasAttr(sequenceproperty))
                {
                    
                    //currentsequencevalue = sequencedoc.getInt(sequenceproperty);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - getting current sequence value",null,null);
                    currentsequencevalue = Integer.parseInt(session.apiGet("get",sequencedoc.getObjectId().getId()+","+sequenceproperty));
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - current sequence value: "+currentsequencevalue,null,null);
                    //sequencedoc.setInt(sequenceproperty,currentsequencevalue+1);
                    session.apiSet("set",sequencedoc.getObjectId().getId()+","+sequenceproperty,""+(currentsequencevalue+1));
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence value incremented",null,null);
                    //sequencedoc.save();
                    //sequencedoc.checkin(false,"");
                    //sequencedoc.cancelCheckout();
                    performed = true;
                }
                boolean bUnlockStatus = session.apiExec("unlock",session.getSessionId()+","+sequencedoc.getObjectId().getId());
                boolean bCommitStatus = session.apiExec("commit",session.getSessionId());
                tries = 10;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence accessed: "+currentsequencevalue,null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
            }
            else
            {
                // cancel transaction before taking a nap...
                boolean bCancelTransaction = session.apiExec("abort",session.getSessionId());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence is busy, waiting and retrying",null,null);
                wait(100+r.nextInt(500)); // nap 100-600 milliseconds...
            }
        }
        if (!performed)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence document was locked after 10 tries...");
            throw new Exception("MrcsSequenceNamingPlugin - sequence doc locked after 10 tries, name generation failed: "+sequencedocname);            
        }
        return currentsequencevalue;
    }

    
    public int getNextSequenceNumberExceptionLoop(IDfSession session, String sequencedocname, String sequenceproperty) throws Exception
    {
        try {
            // get the sequence document
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - getting sequencedoc: "+sequencedocname,null,null);
            IDfSysObject sequencedoc = (IDfSysObject)session.getObjectByPath(sequencedocname);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checking it has sequence prop: "+sequenceproperty,null,null);
            if (!sequencedoc.hasAttr(sequenceproperty))
            {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence property not found on sequence doc");
                throw new Exception("MrcsSequenceNamingPlugin - sequence property "+sequenceproperty+" not found on sequence tracking document: "+sequencedocname);
            }
            // try to do a checkoutlock:
            int tries = 0;
            boolean performed = false;
            int currentsequencevalue = -1;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - attempting sequence document access",null,null);
            while (tries < 10)
            {
                tries++;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checkout try #"+tries,null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
                // start transaction via DMCL API calls
                try { 
                    if (!sequencedoc.isCheckedOut()) 
                    {
                        // lock and perform sequence computation...this isn't perfect, but it's as close as we can get...
                        sequencedoc.checkout();
                        if (sequencedoc.hasAttr(sequenceproperty))
                        {
                            
                            currentsequencevalue = sequencedoc.getInt(sequenceproperty);
                            sequencedoc.setInt(sequenceproperty,currentsequencevalue+1);
                            sequencedoc.save();
                            //sequencedoc.checkin(false,"");
                            sequencedoc.cancelCheckout();
                            performed = true;
                        }
                        tries = 10;
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence accessed: "+currentsequencevalue,null,null);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
                    }
                    else
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence is busy, waiting and retrying",null,null);
                        wait(100+r.nextInt(500)); // nap 100-600 milliseconds...
                    }
                } catch (DfException dfe) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exception thrown during sequence checkout attempt, waiting and retrying",null,null);
                    wait(100+r.nextInt(500)); // nap 100-600 milliseconds...
                }
            }
            if (!performed)
            {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence document was locked after 10 tries...");
                throw new Exception("MrcsSequenceNamingPlugin - sequence doc locked after 10 tries, name generation failed: "+sequencedocname);            
            }
            return currentsequencevalue;
        } catch (DfException dfe) {
            // check df exception so we know if a problem occurred before we even hit the exception/checkout loop
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - DF Exception in sequence access before we even hit the exception loop",dfe);
            throw dfe; // rethrow            
        }
    }

    public int getNextSequenceNumberLockWithExceptionLoop(IDfSession session, String sequencedocname, String sequenceproperty, IDfSessionManager sMgr) throws Exception
    {
        try {
            String docbase = session.getDocbaseName();
            // get the sequence document - to validate sequence property is there... 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - getting sequencedoc: "+sequencedocname,null,null);
            IDfSysObject sequencedoc = (IDfSysObject)session.getObjectByPath(sequencedocname);
            String type = sequencedoc.getTypeName();
            IDfId seqdocid = sequencedoc.getObjectId();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checking it has sequence prop: "+sequenceproperty,null,null);
            if (!sequencedoc.hasAttr(sequenceproperty))
            {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence property not found on sequence doc");
                throw new Exception("MrcsSequenceNamingPlugin - sequence property "+sequenceproperty+" not found on sequence tracking document: "+sequencedocname);
            }
            
            // try to do a checkoutlock:
            int tries = 0;
            boolean performed = false;
            int currentsequencevalue = -1;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - attempting sequence document access",null,null);
            while (tries < 10)
            {
                tries++;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checkout try #"+tries,null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
                try { 
                    // lock and perform sequence computation...this isn't perfect, but it's as close as we can get...
                    //boolean bTransactionStatus = session.apiExec("begintran",session.getSessionId());
                    if (!sMgr.isTransactionActive())
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - starting transaction",null,null);
                        sMgr.beginTransaction();
                    }
                    IDfSession newsession = sMgr.getSession(docbase);
                    sequencedoc = (IDfSysObject)newsession.getObject(seqdocid);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - attempting checkout",null,null);
                    sequencedoc.checkout();
                    currentsequencevalue = sequencedoc.getInt(sequenceproperty);
                    sequencedoc.setInt(sequenceproperty,currentsequencevalue+1);
                    sequencedoc.save(); // this may release the lock...
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - checkout + seq extract successful",null,null);
                    performed = true;
                    sMgr.commitTransaction();
                    sMgr.release(newsession);
                    tries = 10;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence accessed: "+currentsequencevalue,null,null);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exec time: "+new Date().getTime(),null,null);
                } catch (DfException dfe) {
                    try {
                        if (sMgr.isTransactionActive())
                        {
                            sMgr.abortTransaction();                            
                        }
                    } catch (Exception e) {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - attempted to clean up transaction in lock attempt, failed",null,e);                        
                    }
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - exception thrown during sequence checkout attempt, waiting and retrying",null,dfe);
                    synchronized (this) {
                        wait(100+r.nextInt(500)); // nap 100-600 milliseconds...
                    }
                }
            }
            if (!performed)
            {
                /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - sequence document was locked after 10 tries...",null, null);
                throw new Exception("MrcsSequenceNamingPlugin - sequence doc locked after 10 tries, name generation failed: "+sequencedocname);            
            }
            return currentsequencevalue;
        } catch (DfException dfe) {
            // check df exception so we know if a problem occurred before we even hit the exception/checkout loop
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsSequenceNamingPlugin.getNextSequenceNumber - DF Exception in sequence access before we even hit the exception loop",dfe);
            throw dfe; // rethrow            
        }
    }

}