package com.uhg.ewp.common.gotcha.util.vignette7.introspect;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

//import aaa.cms.TomContentRetriever;

import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.AttributeDefinitionData;
import com.vignette.as.client.common.ContentRelationInstance;
import com.vignette.as.client.common.LOBAttributeData;
import com.vignette.as.client.common.ReferenceData;
import com.vignette.as.client.common.ReferenceValueData;
import com.vignette.as.client.common.StaticFileDBQuery;
import com.vignette.as.client.common.StaticFileWhereClause;
import com.vignette.as.client.common.ref.ManagedObjectRef;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.common.ref.ObjectTypeRef;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.ObjectType;
import com.vignette.as.client.javabean.QueryManager;
import com.vignette.as.client.javabean.Site;
import com.vignette.as.client.javabean.StaticFile;
import com.vignette.util.GUID;
import com.vignette.util.StringQueryOp;

public class ContentDetail
{
    public ContentInstance getContentInstance(String sitename, List subchannelpath, String itemname) throws Exception
    {
        Site site = Site.findByName("Broker");
        Channel ch = site.getHomeChannel();
        for (int i=0; i < subchannelpath.size(); i++) {
            String chname = (String)subchannelpath.get(i);
            Channel[] subchs = ch.getAllSubchannels();
            for (Channel subch : subchs) {
                if (chname.equals(subch.getData().getName())) {
                    ch = subch;
                }
            }
        }
        List cimatches = ch.findByName(itemname).asList();
        ContentInstance ci = (ContentInstance)cimatches.get(0);
        
        // retrieve (API bug)
        // Due to a reference issue/bug in the API, re-cast the CI based on its VCMId.
        ci = (ContentInstance)ContentInstance.findByContentManagementId(ci.getContentManagementId());
        return ci;
    }

    
    public String getContentDetail(HttpServletRequest req) throws Exception
    {
        StringBuffer resp = new StringBuffer();
        String guid = req.getParameter("guid");
        
        ContentInstance ci = (ContentInstance)ContentInstance.findByContentManagementId(new ManagedObjectVCMRef(guid));
        
        resp.append("Content Instance Detail <BR>");
        resp.append("Name: "+ci.getName()+"<BR>");
        resp.append("Channel: "+req.getParameter("path")+"<BR>");
        resp.append("Type: "+ci.getObjectType().getName()+"<BR");
        resp.append("<hr>Attributes<BR><hr><BR>");
        
        ObjectType otype = ci.getObjectType();
        AttributeDefinitionData[] attrdefs = otype.getAllAttributeDefinitions(true);
        
        StaticFile sfile = null;
        
        // ATTRIBUTES
        resp.append("<table border=0>");
        for (AttributeDefinitionData attrdef : attrdefs) 
        {
            String attrname = "--";
            Object attrvalobj = null;
            String attrval = "++";
            try { 
                attrname = attrdef.getName();
                AttributeData attrdata = ci.getAttribute(attrdef.getName());
                boolean isLOB = attrdata instanceof LOBAttributeData;
                attrvalobj = ci.getAttributeValue(attrdef.getName());
                attrval = attrvalobj == null ? "<b>NULL<b>" : attrvalobj.toString();
                if (isLOB) attrval += " <i>is LOB</i>";
                if ("file".equals(attrname) && attrval != null) {
                    // attempt to locate the StaticFile MO
                    //String path = attrval.substring(0,attrval.lastIndexOf('/'));
                    String path = attrval;
                    String[] paths = { path };
                    IPagingList pl = StaticFile.findByPlacementPath(paths, null);
                    List sflist = null;
                    if (pl != null) {
                        sflist = pl.asList();
                        attrval += "<BR> -- staticfilecount: "+sflist.size();
                        sfile = (StaticFile)sflist.get(0);
                        
                    } else {
                        attrval += " -- no static file found";
                    }
                }
            } catch (Exception e) {
                int k=0;
                k++;
                k++;
            }
            resp.append("<tr>");
            resp.append("<td>"+attrname+"</td>");
            resp.append("<td>"+attrval+"</td>");
            resp.append("</tr>");
        }
        resp.append("</table>");
        
        // REFERENCES
        resp.append("<BR><hr>References<BR><hr><BR>");
        resp.append("<table border=0>");
        for (ReferenceValueData rvd : ci.getReferences()) 
        {
            String attrname = "--";
            String refcontenttype = "<b>unknown</b>";
            try { 
                ReferenceData rd = rvd.getReference();
                refcontenttype = rd.getReferencedContentType().getContentType().getName();
            } catch (Exception e) {}
            resp.append("<tr>");
            resp.append("<td>"+attrname+"</td>");
            resp.append("<td>"+refcontenttype+"</td>");
            resp.append("</tr>");
        }
        resp.append("</table>");
        
        
        // RELATIONS
        resp.append("<BR><hr>Relations<BR><hr><BR>");
        Hashtable relations = ci.getAllRelations();
        
        resp.append("<table border=0>");
        for (Object o : relations.keySet())
        {
            String clobrel = null;
            String relname = "--";
            try {
                relname = o.toString();
                if (relname.endsWith("-LARGE-OBJECT")) {
                    clobrel = relname;
                }
                //relation = relations.get(o);
                //ContentRelationInstance cri = (ContentRelationInstance)relation;
            } catch (Exception e ){}
            resp.append("<tr>");
            resp.append("<td>"+relname+"</td>");
            if (clobrel == null) {
                resp.append("<td>"+"--"+"</td>");
            } else {
                // get clob content
                Object relation = relations.get(o);
                ContentRelationInstance[] cris = (ContentRelationInstance[])relation;
                
                ContentRelationInstance cri = cris[0];
                String[] attrnames = cri.getAttributeNames();
                String[] relnames = cri.getRelationNames();
                AttributeData clobguid = cri.getAttribute("largeobjectGuid");
                String clgu = clobguid.getValue().toString();
                ManagedObjectVCMRef clobref = new ManagedObjectVCMRef(clgu);
                ObjectTypeRef clobreftype = clobref.getObjectTypeRef();
                ManagedObject clobmo = ManagedObject.findByContentManagementId(clobref);
                //UnmanagedObject unmo = UnmanagedObject.findById(arg0, arg1);
                GUID clobguidobj = new GUID(clgu);
                boolean isvalid = clobguidobj.isValid();
                String typecode = clobguidobj.getTypeCode();
                int a=0;
                a++;
                a++;                
                resp.append("<td>"+"CLOB relation GUID is "+clgu+"</td>");
            }
            resp.append("</tr>");
        }
        resp.append("</table>");
        
        ManagedObjectRef mref = ci.getManagedObjectRef();
        if (mref != null) {
            ManagedObject mobj = ManagedObject.findById(mref);
            boolean isStaticFile = mobj instanceof StaticFile;
            resp.append("<BR><HR><BR>");
            resp.append("Man Obj Ref name: "+mobj.getName()+"<BR>");
            resp.append("isStatic: "+isStaticFile+"<BR>");
            resp.append("isCI: "+(mobj instanceof ContentInstance)+"<BR>");
        }
        if (sfile != null) {
            resp.append("<a href='ContentStatic?guid="+sfile.getContentManagementId().getId()+"'>static file</a><br>");
        }

        boolean doquery = false;
        if (doquery) {
            try {
                String cipath = req.getParameter("path");
                // Create the where clause to check status and placement path
                StaticFileWhereClause clause = new StaticFileWhereClause();
                //clause.checkStatus(StringQueryOp.EQUAL, "Approved");
                clause.checkPlacementPath(StringQueryOp.STARTS_WITH, cipath);
                // Create query, ordered by placement path
                StaticFileDBQuery query = new StaticFileDBQuery();
                query.setWhereClause(clause);
                //query.addOrderByPlacementPath();
                // Add the return attributes which will be set in the content instances.
                query.addReturnsPlacementPath();
                // Run the query
                IPagingList results = QueryManager.execute(query);
                // Print out all of the results
                int size = results.size();
                System.out.println("Results returned - " + size);
                List list = results.asList();
                Iterator iterator = list.iterator();
                
                while (iterator.hasNext()) {
                    StaticFile obj = (StaticFile)iterator.next();
                    resp.append("-- placement path: " + obj.getPlacementPath() + " -- <BR>");
                }
            
            } catch (Exception e) {
                
            }
        }
        
        {
            
            try {
                resp.append("<hr>");
                Map tomgriffinattrs = TomContentRetriever.getAllAttributes(ci);
                Iterator it = tomgriffinattrs.keySet().iterator();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    Object value = tomgriffinattrs.get(name);
                    resp.append("TGAttr -- "+name+" -- "+value.toString()+"<br>");
                }
                
            } catch (Exception e) {
                
            }
        }
        
        
        return resp.toString();
        
    }
    
}
