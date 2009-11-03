package com.uhg.umvs.bene.cms.contentretrieval.contentsource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;

public class FileSystemSource implements ContentSource
{
    String baseroot;
    public void setBaseroot(String baseroot) { this.baseroot = baseroot; }

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
                OutputStream os = resp.getOutputStream();
                int read = 0;
                byte[] bytes = new byte[1024];
           
                //While there are still bytes in the file, read them and write them to our OutputStream
                while((read = fis.read(bytes)) != -1){
                   os.write(bytes,0,read);
                }

                //Clean resources
                os.flush();
                os.close();
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
