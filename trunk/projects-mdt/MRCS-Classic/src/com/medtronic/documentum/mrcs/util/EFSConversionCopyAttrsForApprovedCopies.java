/*
 * Created on Feb 27, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfValue;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFSConversionCopyAttrsForApprovedCopies
{        
    String g_relationname = "MrcsPdfRenditionCopyRelationship";
    
    String g_cabinet = "/CRM/TDS EFS";
    
    String g_mqa_doctype = "m_mrcs_mqa_document";
    String[] g_mqa_attrlist = {        
        "title", 
        "authors", 
        "m_mrcs_mqa_part_number", 
        "m_mrcs_mqa_author_date", 
        "m_mrcs_mqa_document_type", 
        "m_mrcs_mqa_supplier", 
        "keywords"
    };
    String g_eps_doctype = "m_mrcs_eps_document";
    String[] g_epsys_attrlist = {            
        "title", 
        "authors", 
        "m_mrcs_epsys_device_type", 
        "m_mrcs_epsys_model_number", 
        "m_mrcs_epsys_doc_type", 
        "m_mrcs_epsys_phase", 
        "keywords", 
        "m_mrcs_epsys_product_name", 
        "m_mrcs_epsys_project_name"
    };
    String g_cf_doctype = "m_mrcs_central_document";
    String[] g_cf_attrlist = {            
        "title", 
        "authors", 
        "keywords", 
        "m_mrcs_central_author_date", 
        "m_mrcs_central_dhf", 
        "m_mrcs_central_doc_type", 
        "m_mrcs_central_func_group", 
        "m_mrcs_central_phase", 
        "m_mrcs_central_ref_number"
    };
    
    public static void main (String[] args) throws Exception
    {
        String docbase = args[0];
        String username = args[1];
        String password = args[2];
        
        IDfClient client = new DfClient();
        IDfSessionManager sMgr = client.newSessionManager();

        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(username);
        loginInfoObj.setPassword(password);
        loginInfoObj.setDomain(null);

        sMgr.setIdentity(docbase, loginInfoObj);
        
        // transactionalize the session Manager?
        
        IDfSession session = sMgr.getSession(docbase);        
        
        EFSConversionCopyAttrsForApprovedCopies istc = new EFSConversionCopyAttrsForApprovedCopies();       
        
        istc.doEFSAttrConvert(session);
        
        sMgr.release(session);                        
        
        
    }
    
    
    public void doEFSAttrConvert(IDfSession session) throws DfException
    {
        String relationname = g_relationname; 
        
        HashMap attrmap = new HashMap();
        attrmap.put(g_mqa_doctype,g_mqa_attrlist);
        attrmap.put(g_eps_doctype,g_epsys_attrlist);
        attrmap.put(g_cf_doctype,g_cf_attrlist);
        
        String cabinet = g_cabinet;
        
                
        fixApprovedCopyAttributes(session, relationname, attrmap, cabinet);
    }
    
    
    public void fixApprovedCopyAttributes(IDfSession session, String relationname, Map attrlistmap, String cabinet) throws DfException
    {
        /*-CFG-*/String m="fixApprovedCopyAttributes-";
        IDfId childId = null;
        IDfId parentId = null;
        IDfCollection myObj1 = null;
        
        try{
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"composing qualification",null,null);
            String qualification =  
                "select child_id, parent_id from dm_relation "+
                "where relation_name = '"+relationname+"' AND "+
                      "parent_id in (select r_object_id from m_mrcs_document where folder('"+cabinet+"', descend))";
            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DQL query : "+qualification,null,null);
            qry.setDQL(qualification);    
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"prequery: "+(new Date()).getTime() ,null,null);
            myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"postquery: "+(new Date()).getTime() ,null,null);    
            // look up the object we just located (should only be one or none?)
            while(myObj1.next()) 
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"next retrieved object",null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"what is the child id?",null,null);
                // get childid
                String childidattr = myObj1.getString("child_id");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- childid: "+childidattr,null,null);
                childId = new DfId(childidattr);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"what is the parent id?",null,null);
                // get parentid
                String parentidattr = myObj1.getString("parent_id");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- parentid: "+parentidattr,null,null);
                parentId = new DfId(parentidattr);
                
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"retrieving child doc obj",null,null);
                IDfDocument childDoc = (IDfDocument)session.getObject(childId);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"retrieving parent doc obj",null,null);
                IDfDocument parentDoc = (IDfDocument)session.getObject(parentId);
                
                // determine doctype so we can get type-specific attr list to clone
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting parent DCTM doctype",null,null);
                String parenttype = parentDoc.getTypeName();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- parent doctype: "+parenttype,null,null);
                                
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up associated attr list",null,null);
                if (attrlistmap.containsKey(parenttype))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling copy attributes method for this attr list",null,null);
                    String[] attrlist = (String[])attrlistmap.get(parenttype);
                    copyAttributes(session,parentDoc,childDoc,attrlist);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copy attributes method call completed",null,null);
                }
                else // log error, but don't stop?
                {
                    /*-ERROR-*/DfLogger.error(this,m+"unknown document type - "+parenttype+" - parent id - "+parentidattr+" - child id - "+childidattr,null,null);
                }
            }
            myObj1.close();

        }catch(Exception e){
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"Error while looking up child id",e);
        } finally {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"closing collection",null,null);
            if (myObj1 != null){ myObj1.close(); }
        }
        
    }
    
    public void copyAttributes(IDfSession session, IDfDocument parentDoc, IDfDocument childDoc, String[] attrlist) throws DfException
    {
        // copy MRCS attrs if possible
        /*-CFG-*/String m="copyAttributes-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for mrcs_application attr",null,null);
        if (parentDoc.hasAttr("mrcs_application"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copying mrcs_application attr",null,null);
            childDoc.setString("mrcs_application",parentDoc.getString("mrcs_application"));
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for mrcs_config attr",null,null);
        if (parentDoc.hasAttr("mrcs_config"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copying mrcs_config attr",null,null);
            childDoc.setString("mrcs_config",parentDoc.getString("mrcs_config"));
        }
        // process custom attr list        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing custom attr copy list",null,null);
        for (int i=0; i < attrlist.length; i++)
        {
            String attrname = (String)attrlist[i];
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attribute to copy: "+attrname,null,null);
            
            boolean isRepeating = parentDoc.isAttrRepeating(attrname);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--repeating? "+isRepeating,null,null);
            if (isRepeating) 
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterate through repeating values and copy each",null,null);
                for (int r=0; r < parentDoc.getValueCount(attrname); r++)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--looking up value #"+r,null,null);
                    IDfValue v = parentDoc.getRepeatingValue(attrname,r);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--setting value ["+v.asString()+"]",null,null);
                    childDoc.setRepeatingValue(attrname,r,v);
                }                
            } else {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"performing single-valued copy",null,null);
                IDfValue attrvalue = parentDoc.getValue(attrname);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--setting value ["+attrvalue.asString()+"]",null,null);
                childDoc.setValue(attrname,attrvalue);                
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attr copy complete",null,null);
    }
    
    
}

