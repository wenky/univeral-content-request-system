package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

public class CheckVersionTree extends AbstractDocumentProcessor {

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception
    {
        // check that chronicle id document exists (alpha)
        /*-dbg-*/Lg.dbg("Verify document integrity and version tree");
        checkChronicleId(access,document,script,context);
        
        checkVersionTree(access,document,script,context);
        
    }
    
    
    public static void checkChronicleId(DctmAccess access, Map document, Map script, Context context) throws Exception
    {
        // make sure chronicleid doc version is there
        /*-dbg-*/Lg.dbg("Verify chronicle id is retrievable");
        String chronid = (String)document.get("ChronicleId");
        /*-trc-*/Lg.trc("chronid found? ",chronid);        
        IDfSession session = null;
        try {
            session = access.getSession();

            IDfId theid = null;
            /*-trc-*/Lg.trc("make it a dfid");        
            try { theid = new DfId(chronid); } 
                catch (Exception e) { throw new DocumentError("CheckVersionTree - checkChronicleId - Invalid chronicle id: "+chronid,e,document); }            
            
            /*-trc-*/Lg.trc("look up the idfsysobject");        
            IDfSysObject thedoc = null;
            try {thedoc = (IDfSysObject)session.getObject(new DfId(chronid)); }
                catch (ClassCastException cce) { throw new DocumentError("CheckVersionTree.checkChronicleId - Not a sysobject, perhaps persistent object: "+chronid,cce,document); }
                catch (DfException dfe) { throw new DocumentError("CheckVersionTree.checkChronicleId - DCTM error on lookup: "+chronid + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
                catch (Exception e) { throw new DocumentError("CheckVersionTree.checkChronicleId - Other error on lookup: "+chronid + " err: " + e.getMessage(),e,document); }

            if (thedoc == null) {
                throw new DocumentError("CheckVersionTree.checkChronicleId - sysobject not found for chronid: "+chronid,document); 
            } else {
                /*-dbg-*/Lg.dbg("idfsysobject located for chronid %s",chronid);        
                document.put("ChronicleDocumentVerified", true);
            }
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
        /*-dbg-*/Lg.dbg("!!!SUCCESS!!!");        
        
    }

    public static void checkVersionTree(DctmAccess access, Map document, Map script, Context context) throws Exception
    {
        // make sure chronicleid doc version is there
        /*-dbg-*/Lg.dbg("Verify and retrieve version tree");
        String chronid = (String)document.get("ChronicleId");
        IDfSession session = null;
        try {
            session = access.getSession();
            
            // get most current 
            /*-trc-*/Lg.trc("get topmost/current idfsysobject for chronid %s",chronid);
            IDfSysObject curdoc = (IDfSysObject)session.getObjectByQualification("dm_sysobject where i_chronicle_id = '"+chronid+"'");
            // get largest docid
            /*-trc-*/if(Lg.trc())Lg.trc("most current objectid: %s",curdoc==null?null:curdoc.getObjectId().getId());

            
            /*-trc-*/Lg.trc("get maxid to check if current version == max id");
            IDfValue maxid = DctmUtils.execSingleValueQuery(session,"SELECT max(r_object_id) from dm_sysobject(ALL) where i_chronicle_id = '"+chronid+"'");
            
            /*-trc-*/Lg.trc("compare curid with maxid, warn if failure");
            if (!DctmUtils.getValueAsString(maxid).equals(curdoc.getObjectId().getId())) {
                /*-WARN-*/Lg.wrn("-->WARNING: maxid bigger than curid for chronid %s",chronid);
            }
            
            // get list of objects for this chronicle id
            /*-trc-*/Lg.trc("get list of objects associated with this chronicle id");
            List objectlist = DctmUtils.execQuery(session, "SELECT r_object_id, object_name,i_antecedent_id,i_branch_cnt  FROM dm_sysobject(ALL) where i_chronicle_id = '"+chronid+"'");
            List idlist = new ArrayList(objectlist.size());
            Map refmap = new HashMap();
            // get docobject, add version numbers, validate not branched...
            /*-trc-*/Lg.trc("iterate through the objectlist to get objectid and version numbers");
            for (int i=0; i < objectlist.size(); i++)
            {
                
                /*-trc-*/Lg.trc("doc version iteration #%d",i);
                Map object = (Map)objectlist.get(i);
                String id = (String)object.get("r_object_id");
                /*-dbg-*/Lg.dbg("current version id: %s",id);
                if (((Integer)object.get("i_branch_cnt")) > 0)
                {
                    /*-trc-*/Lg.trc("BRANCH detected on document with chronid %s curid %s",chronid,id);
                    throw new DocumentError("CheckVersionTree.checkVersionTree - branch versions detected on doc with id: "+id + " chronid: " + chronid,document);
                }
                idlist.add(id);
                
                /*-trc-*/Lg.trc("lookup sysobject");
                IDfSysObject so; try { so = (IDfSysObject)session.getObject(new DfId(id)); } 
                    catch (DfException dfe) { throw new DocumentError("CheckVersionTree.checkVersionTree - DCTM error on lookup of docobject for docid: "+id + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
                    catch (Exception e) { throw new DocumentError("CheckVersionTree.checkVersionTree - error on lookup of docid: "+id + " err: "+e.getMessage(),e,document); }
                /*-trc-*/Lg.trc("get version of sysobject %s",so==null?null:so.getObjectId().getId());
                try { object.put("version", DctmUtils.getVersionNumber(so)); }
                    catch (DfException dfe) { throw new DocumentError("CheckVersionTree.checkVersionTree - DCTM error on lookup of version number for docid: "+id + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
                    catch (Exception e) { throw new DocumentError("CheckVersionTree.checkVersionTree - error on lookup of version number for  docid: "+id + " err: "+e.getMessage(),e,document);  }
                refmap.put(id, object);
            }
            
            /*-trc-*/Lg.trc("sort idlist");            
            Collections.sort(idlist);
            
            // perform antecedent descent
            List antelist = new ArrayList();
            IDfSysObject doc = curdoc;
            while (true) {
                /*-trc-*/Lg.trc("get current id");
                String curid = doc.getObjectId().getId();
                /*-trc-*/Lg.trc("make sure id %s is in the queried list of versions", curid);
                if (!refmap.containsKey(curid)) {
                    // oooh, that's bad...
                    throw new DocumentError("CheckVersionTree.CheckVersionTree - antecedent descent found object id that's not in the chronicle query, chronid: "+chronid + " id: " + curid ,document); 
                }
                /*-trc-*/Lg.trc("add to antelist");
                antelist.add(0,curid);
                /*-trc-*/Lg.trc("readched the bottom? ");
                if (curid.equals(chronid)) {
                    /*-trc-*/Lg.trc("curid == chronid, done");                    
                    break;
                }else {
                    /*-trc-*/Lg.trc("descent to next");                    
                    String antecedentid = doc.getAntecedentId().getId();
                    /*-trc-*/Lg.trc("lookup antecedent %s",antecedentid);                    
                    try { doc = (IDfSysObject)session.getObject(doc.getAntecedentId()); }
                        catch (DfException dfe) { throw new DocumentError("CheckVersionTree.checkVersionTree - DCTM error in antecedent version descent check on lookup of docid: "+antecedentid + " for chronicle id: "+chronid+" err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); }
                        catch (Exception e) { throw new DocumentError("CheckVersionTree.checkVersionTree - error in antecedent version descent check on lookup of docid: "+antecedentid + " for chronicle id: "+chronid+" err: "+ e.getMessage(),e,document);  }
                    /*-trc-*/Lg.trc("found? %b"+(doc!=null));                    
                }
            }
            
            
            /*-trc-*/Lg.trc("sort by version numbers");                    
            Object[] labelsort = new Object[objectlist.size()];
            {
                for (int c=0; c < objectlist.size(); c++) {
                    Map curobj = (Map)objectlist.get(c); 
                    Object[] arr = {curobj.get("r_object_id"),curobj.get("version")}; 
                    labelsort[c] = arr;
                }
            }
            class comp implements Comparator { 
                public int compare(Object o1, Object o2) {
                    String a=(String)((Object[])o1)[1];
                    String b=(String)((Object[])o2)[1];
                    int maja = Integer.parseInt(a.substring(0,a.indexOf('.')));
                    int majb = Integer.parseInt(b.substring(0,b.indexOf('.')));
                    if (maja > majb) return 1; else if (majb > maja) return -1; 
                    int mina = Integer.parseInt(a.substring(a.indexOf('.')+1));
                    int minb = Integer.parseInt(b.substring(b.indexOf('.')+1));
                    if (mina > minb) return 1; else if (minb > mina) return -1; else return 0; 
                }                
                public boolean equals(Object obj) {return false;} 
            }                        
            Arrays.sort(labelsort, new comp());
            
            // now audit all these sorted versions for: 
            // - matching result count (objevctlist == antelist)
            /*-trc-*/Lg.trc("check that idlist's size equals antelists's");                    
            if (idlist.size() > antelist.size()) {
                /*-trc-*/Lg.trc("mismatch (idlist is larger) - determining missing ids");                    
                StringBuffer missing = null;
                for (int i=0; i < idlist.size(); i++) {
                    if (!antelist.contains(idlist.get(i))) {
                        if (missing == null) missing = new StringBuffer((String)idlist.get(i)); else missing.append(',').append(idlist.get(i));
                    }
                }
                /*-WARN-*/Lg.wrn("document %s is missing ids from antecedent list: %s",chronid,missing);                    
                throw new DocumentError("CheckVersionTree.CheckVersionTree - version count mismatch between antecedent list and i_chronicle_id query: "+chronid + " ids: " + missing.toString() ,document); 
            }
            if (antelist.size() > idlist.size()) {
                /*-trc-*/Lg.trc("mismatch (antecedent is larger...huh?)");                    
                StringBuffer missing = null;
                for (int i=0; i < antelist.size(); i++) {
                    if (!idlist.contains(antelist.get(i))) {
                        if (missing == null) missing = new StringBuffer((String)antelist.get(i)); else missing.append(',').append(antelist.get(i));
                    }
                }
                /*-WARN-*/Lg.wrn("antecedent list for document %s has ids not found in chronicleid query: %s",chronid,missing);                    
                throw new DocumentError("CheckVersionTree.CheckVersionTree - version count mismatch between i_chronicle_id query and antecedent list: "+chronid + " ids: " + missing.toString() ,document); 
            }
            // - antelist order matches label version order)
            { 
                /*-trc-*/Lg.trc("check that antecedent order matches version label order");                    
                for (int c=0; c < antelist.size(); c++) {
                    String anteid = (String)antelist.get(c);
                    String verid = (String)((Object[])labelsort[c])[0];
                    String ver = (String)((Object[])labelsort[c])[1];
                    if (!anteid.equals(verid)) {
                        /*-WARN-*/Lg.wrn("antecedent list order doesn't match the version label order, anteid %s, verid %s",anteid,verid);                    
                        throw new DocumentError("CheckVersionTree.CheckVersionTree - id mismatch between antecedent list and version sort: "+chronid + " ante id: " + anteid + " ver id: " + verid + " version label: " + ver + " index: " + c, document); 
                    }
                }
            }
            
            // - antelist matches docid actual sort list (more of a warning...)            
            { 
                /*-trc-*/Lg.trc("check that object id order matches antecedent list order (admin work could mess this up)");                    
                for (int c=0; c < antelist.size(); c++) {
                    String anteid = (String)antelist.get(c);
                    String docid = (String)idlist.get(0);
                    if (!anteid.equals(docid)) {
                        /*-INFO-*/Lg.inf("antecedent list order doesn't match raw objectid order",anteid,docid);
                        break;
                    }
                }
            }
            
            // it's passed muster, so repackage the antelist (the proper/ideal order) as the serializable version list
            { 
                /*-trc-*/Lg.trc("place list in document map as Versions");                    
                for (int c=0; c < antelist.size(); c++) {
                    String anteid = (String)antelist.get(c);
                    Map object = (Map)refmap.get(anteid);
                    antelist.set(c,object);
                }                
                document.put("Versions", antelist);
            }
            
        } catch (DfException dfe) { 
            /*-ERROR-*/Lg.err("CheckVersionTree.CheckVersionTree - DCTM error on lookup: "+chronid + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe);                    
            throw new DocumentError("CheckVersionTree.CheckVersionTree - DCTM error on lookup: "+chronid + " err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document); 
        } catch (Exception e) { 
            /*-ERROR-*/Lg.err("CheckVersionTree.CheckVersionTree - Other error on lookup: "+chronid + " err: " + e.getMessage(),e);                    
            throw new DocumentError("CheckVersionTree.CheckVersionTree - Other error on lookup: "+chronid + " err: " + e.getMessage(),e,document);
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
        /*-dbg-*/Lg.dbg("done %s",chronid);                    
        
    }

}
