package com.uhg.umvs.bene.cms.contentretrieval.contentsource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.umvs.bene.cms.contentretrieval.interfaces.ContentSource;

// gets resource/content item relative to a configured path on the local filesystem
// - should be usable against Vignette Deployment Agent-placed content 

public class FileSystemSource implements ContentSource
{
    String baseroot;
    public void setBaseroot(String baseroot) { this.baseroot = baseroot; }

    public boolean hasContent(String contentItem, HttpServletRequest request)
    {
        String filepath = baseroot + contentItem;
        File thefile = new File(filepath);
        return thefile.exists();        
    }

    
    public void getContent(String contentItem, HttpServletRequest req, HttpServletResponse resp)
    {
        // form filename from baseroot
        String filepath = baseroot + contentItem;
        File thefile = new File(filepath);
        if (!thefile.exists()) {
            throw new RuntimeException("FSSource: File "+filepath+" not found");
        } else if (!thefile.isFile()) {
            throw new RuntimeException("FSSource: File "+filepath+" not a file");
        } else {
            // determine mimetype, ?default to html?
            String mimetype = determineMimeType(thefile);            
            // write file to response
            //byteArrayStream.writeTo(response.getOutputStream())
            try { 
                BufferedInputStream fis = new BufferedInputStream(new FileInputStream(thefile));
                IOUtils.copy(fis, resp.getOutputStream());
                resp.getOutputStream().flush();
                fis.close();
            } catch (IOException ioe) {
                throw new RuntimeException("FSSource: Error reading file "+filepath+" to http response",ioe);
            }
        }
        
    }
    
    public String determineMimeType(File thefile)
    {
        String mimetype = new MimetypesFileTypeMap().getContentType(thefile);
        return mimetype;
    }
    

}
