package com.medtronic.ecm.documentum.core.plugins.naming;

import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.util.UnitTest;

public class MdtMiniSequenceNaming extends MdtSequenceNaming 
{
    public String generateName(String docbase, IDfSysObject object, Map config)
    {
        try { 
            // check if minisequence exists
            IDfFolder folder = (IDfFolder)object.getSession().getObject(object.getFolderId(0));
            String folderpath = folder.getFolderPath(0);
            String seqtype = (String)config.get("SequenceDocType");
            String seqname = (String)config.get("SequenceDocName");
            IDfSysObject seq = (IDfSysObject)object.getSession().getObjectByQualification(seqtype+" where FOLDER('"+folderpath+"') and object_name = '"+seqname+"'");
            if (seq == null) {
                // need to create new minisequence in this folder
                seq = newMiniSequence(object.getSession(),seqtype,seqname,folderpath);
            }
            Map cfg2 = new HashMap(config);
            cfg2.put("SequenceDocName",folderpath+"/"+seqname);            
            String name = super.generateName(docbase, object, cfg2);
            return name;
        } catch (DfException dfe) {
            // check df exception so we know if a problem occurred before we even hit the exception/checkout loop
            /*-ERROR-*/Lg.err("DFException in getting minisequence",dfe);
            throw EEx.create("NAME-MiniSeq-DFE", "DFException in getting minisequence",dfe);
        }            
    }
    
    public IDfSysObject newMiniSequence(IDfSession session, String type, String name, String folder) throws DfException
    {
        IDfSysObject sequence = (IDfSysObject)session.newObject(type);
        sequence.setObjectName(name);
        sequence.link(folder);
        sequence.setHidden(true);
        sequence.save();
        return sequence;
    }

    public static void main(String[] args) throws Exception 
    {
        IDfSessionManager smgr = UnitTest.getSessionManager("ecsadmin","spring2005","mqadoc_dev");
        IDfSession session = smgr.getSession("mqadoc_dev");
        
        Map cfg = new HashMap();
        cfg.put("SequenceDocName", "NMNMiniSequence");
        cfg.put("SequenceDocType", "mdt_sequence");
        cfg.put("SequenceProperty", "m_sequence_num");
        cfg.put("Format", "0000");
        IDfSysObject so = (IDfSysObject)session.getObject(new DfId("09017f44800bff7d"));
        
        MdtMiniSequenceNaming m = new MdtMiniSequenceNaming();
        String name = m.generateName("mqadoc_dev", so, cfg);
        
        int i=0; 
        i++;
    }
    

}
