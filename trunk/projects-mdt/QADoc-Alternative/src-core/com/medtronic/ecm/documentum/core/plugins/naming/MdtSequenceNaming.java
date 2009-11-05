package com.medtronic.ecm.documentum.core.plugins.naming;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtDocumentNaming;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.6 $
 * 
 * 
 */ 
public class MdtSequenceNaming implements IMdtDocumentNaming 
{
    Random r = new Random(new Date().getTime());
    
    /**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param docbase <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param object <font color="#0000FF"><b>(IDfSysObject)</b></font> TODO:
    * @param context <font color="#0000FF"><b>(Object)</b></font> TODO:
    * @return <font color="#0000FF"><b>String</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
    public String generateName(String docbase, IDfSysObject object, Map configdata)
    {
        /*-INFO-*/Lg.inf("top of plugin");
        /*-dbg-*/Lg.dbg("getting configuration properties");
        String sequencedocname = (String)configdata.get("SequenceDocName");
        /*-dbg-*/Lg.dbg("seq doc name: %s",sequencedocname);
        String sequenceproperty = (String)configdata.get("SequenceProperty");
        /*-dbg-*/Lg.dbg("seq doc prop: %s",sequenceproperty);
        IDfSession session  = null;
        int seqnbr = this.getNextSequenceNumberLockWithExceptionLoop(docbase,object.getSessionManager(),sequencedocname,sequenceproperty);
        /*-dbg-*/Lg.dbg("seq number: %d",seqnbr);
        String format = (String)configdata.get("Format");
        /*-dbg-*/Lg.dbg("postprocess format: %s",format);
        String name = postprocess(seqnbr, format);
        /*-dbg-*/Lg.dbg("name post-process: "+name);
        String sequenceprefixkey = (String)configdata.get("CustomPrefixKey");
        String sequencesuffixkey = (String)configdata.get("CustomSuffixKey");
        if (sequenceprefixkey != null)
        {
            /*-dbg-*/Lg.dbg("custom prefix enabled");
            String prefix = (String)configdata.get(sequenceprefixkey);
            if (prefix != null)
            {
                name = prefix+name;
            }
        }
        if (sequencesuffixkey != null)
        {
            /*-dbg-*/Lg.dbg("custom suffix enabled");
            String suffix = (String)configdata.get(sequencesuffixkey);
            if (suffix != null)
            {
                name = name+suffix;
            }
        }
        /*-dbg-*/Lg.dbg("final name: "+name);
        return name;
    	
    }
    
    
    /**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param docbase <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param sMgr <font color="#0000FF"><b>(IDfSessionManager)</b></font> TODO:
    * @param sequencedocname <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param sequenceproperty <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>Integer</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
    public int getNextSequenceNumberLockWithExceptionLoop(String docbase, IDfSessionManager sMgr, String sequencedocname, String sequenceproperty)
    {
        /*-INFO-*/Lg.inf("Begin sequence extraction with locking and exception looping (10 tries)");
    	IDfSession session = null;
        try {
        	session = sMgr.getSession(docbase);
            // get the sequence document - to validate sequence property is there... 
            /*-dbg-*/Lg.dbg("getting sequencedoc: %s",sequencedocname);
            IDfSysObject sequencedoc = (IDfSysObject)session.getObjectByPath(sequencedocname);
            String type = sequencedoc.getTypeName();
            IDfId seqdocid = sequencedoc.getObjectId();
            /*-dbg-*/Lg.dbg("checking it has sequence prop: %s",sequenceproperty);
            if (!sequencedoc.hasAttr(sequenceproperty))
            {
                /*-ERROR-*/Lg.err("sequence property "+sequenceproperty+" not found on sequence tracking document: "+sequencedocname);
                throw EEx.create("NAME-SeqNbr-AttrNotFound","sequence property "+sequenceproperty+" not found on sequence tracking document: "+sequencedocname);
            }
            
            // try to do a checkoutlock:
            int tries = 0;
            boolean performed = false;
            int currentsequencevalue = -1;
            /*-dbg-*/Lg.dbg("attempting sequence document access");
            while (tries < 10)
            {
                tries++;
                /*-dbg-*/Lg.dbg("checkout try #"+tries);
                /*-dbg-*/Lg.dbg("exec time: "+new Date().getTime());
                try { 
                    // lock and perform sequence computation...this isn't perfect, but it's as close as we can get...
                    if (!sMgr.isTransactionActive())
                    {
                        /*-dbg-*/Lg.dbg("starting transaction");
                        sMgr.beginTransaction();
                    }
                    IDfSession newsession = sMgr.getSession(docbase);
                    sequencedoc = (IDfSysObject)newsession.getObject(seqdocid);
                    /*-dbg-*/Lg.dbg("attempting checkout");
                    sequencedoc.checkout();
                    currentsequencevalue = sequencedoc.getInt(sequenceproperty);
                    sequencedoc.setInt(sequenceproperty,currentsequencevalue+1);
                    sequencedoc.save(); // this may release the lock...
                    /*-dbg-*/Lg.dbg("checkout + seq extract successful");
                    performed = true;
                    sMgr.commitTransaction();
                    tries = 10;
                    /*-dbg-*/Lg.dbg("sequence accessed: "+currentsequencevalue);
                    /*-dbg-*/Lg.dbg("exec time: "+new Date().getTime());
                } catch (DfException dfe) {
                    /*-dbg-*/Lg.dbg("error in current increment attempt, trying to clean up transaction");
                    try {if (sMgr.isTransactionActive())sMgr.abortTransaction();} catch (Exception e) {Lg.wrn("attempted to clean up transaction in lock attempt, failed");}
                    /*-dbg-*/Lg.dbg("exception thrown during sequence checkout attempt, waiting and retrying (tries: %d)",tries,dfe);
                    synchronized (this) {
                    	try { 
                    		wait(100+r.nextInt(500)); // nap 100-600 milliseconds...
                    	} catch (InterruptedException ei) {
                            /*-ERROR-*/Lg.err("Interruption error while waiting for sequence number lock to clear on try %d",tries,ei);
                            throw EEx.create("NAME-SeqNbr-INTERRUPT","Interruption error while waiting for sequence number lock to clear on try %d",tries,ei);            
                    	}
                    }
                }
            }
            if (!performed)
            {
                /*-ERROR-*/Lg.err("sequence document was locked after 10 tries...");
                throw EEx.create("NAME-SeqNbr-TIMEOUT","sequence document was locked after 10 tries...");            
            }
            return currentsequencevalue;
        } catch (DfException dfe) {
            // check df exception so we know if a problem occurred before we even hit the exception/checkout loop
            /*-ERROR-*/Lg.err("DFException in sequence access before encountering exception loopback/retry",dfe);
            throw EEx.create("NAME-SeqNbr-DFE", "DFException in sequence access before encountering exception loopback/retry",dfe);
        } finally {
			/*-trc-*/Lg.trc("release session in finally clause...");
			try {if (session != null)sMgr.release(session);} catch (Exception ee) {Lg.wrn("session could not be released in catch");}
		}
    }

    
    /**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param seqnbr <font color="#0000FF"><b>(Integer)</b></font> TODO:
    * @param format <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>String</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
    public String postprocess(int seqnbr, String format)
    {
        /*-INFO-*/Lg.inf("postprocessing %d to format %s",seqnbr,format);
        if (format != null)
        {
            /*-dbg-*/Lg.dbg("pattern: %s",format);
            DecimalFormat formatter = new DecimalFormat(format);
            String output = formatter.format(seqnbr);
            /*-dbg-*/Lg.dbg("output of formatter: %s",output);
            return output;
        }
        else
        {
            /*-dbg-*/Lg.dbg("no formatting pattern found");
            return ""+seqnbr;
        }
    }

}
