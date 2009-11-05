package com.nighteclipse.zoder.processors;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.nighteclipse.zoder.util.DOMUtils;
import com.nighteclipse.zoder.util.ZuluDateFormatter;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;
import com.zoder.util.Lg;


public class MMMAuditHistoryProcessor extends AbstractVersionProcessor
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            String basedql = (String)context.get("DQL");
            /*-log-*/Lg.log("compute actual dql from base");
            if (basedql.toUpperCase().indexOf("WHERE") == -1)
                basedql += " WHERE audited_obj_id = '"+so.getObjectId().getId()+"'";
            else 
                basedql += " AND audited_obj_id = '"+so.getObjectId().getId()+"'";
            
            Element d = (Element)version.get("DOM");
            
            IDfQuery qry = new DfQuery();
            qry.setDQL(basedql);
            IDfCollection myObj1 = null;
            try { 
                /*-log-*/Lg.log("exec "+basedql);
                myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
                Node historyListElement = DOMUtils.getmake(d,"historyList");
                while (myObj1.next()) 
                {
                    /*-log-*/Lg.log("processing next history row");
                    // iterate on columns, look for attr remaps in config
                    Node histrec = DOMUtils.newElement(d, "history");
                    for (int i=0; i < myObj1.getAttrCount(); i++)
                    {
                        String attrname = myObj1.getAttr(i).getName();
                        if (context.containsKey(attrname))
                        {
                            String targetname = (String)context.get(attrname);
                            String attrval = "";
                            if (myObj1.getAttrDataType(attrname) == IDfAttr.DM_TIME)
                            {
                                attrval = ZuluDateFormatter.formatDate(myObj1.getTime(attrname).getDate());
                            } else {
                                attrval = myObj1.getValue(attrname).asString();
                            }
                            DOMUtils.put(histrec,targetname, attrval);
                        }
                    }
                    historyListElement.appendChild(histrec);
                }
            } finally { 
                try {myObj1.close();} catch (Exception e){}
            }       
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }


}
