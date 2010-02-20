package com.uhg.ewp.common.gotcha.util.vignette7.introspect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.common.ref.StaticFileRef;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentManagementOps;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.StaticFile;

public class ContentStatic extends HttpServlet
{

    @Override
    protected void service(HttpServletRequest httpreq, HttpServletResponse httpresp) throws ServletException, IOException
    {
        String guid = httpreq.getParameter("guid");
        
        try { 
            //47d272ff1ac7f010VgnVCM100000c520720aSTFL
            StaticFile sfile = null;
            ManagedObject mo = ManagedObject.findByContentManagementId(new ManagedObjectVCMRef(guid));
            if (mo instanceof ContentInstance) {
                ContentInstance ci = (ContentInstance)mo;
                // try to locate the StaticFile corresponding to the CI
                String name = ci.getName();
                ManagedObjectVCMRef[] refs = ci.findAllReferences(false);
                if (refs != null) {
                    for (ManagedObjectVCMRef ref : refs) {
                        ManagedObject refmo = ManagedObject.findByContentManagementId(ref);
                        if (refmo instanceof StaticFile) {
                            sfile = (StaticFile)refmo;
                            break;
                        }
                    }
                }
            }
            if (mo instanceof StaticFile) {
                sfile = (StaticFile)mo;
            }
                        
            
            StaticFileRef sfr = new StaticFileRef(sfile);
            InputStream is = ContentManagementOps.getStaticFileContentsToInputStream(sfr);
            OutputStream out = httpresp.getOutputStream();
            
            IOUtils.copy(is, out);
            out.flush();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    
    
    
}
