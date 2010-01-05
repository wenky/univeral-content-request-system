package com.uhg.umvs.bene.cms.contentretrieval.requestserver.contentsource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

// get content from an FTP server. need to configure a server. optionally provide username, password, 
// and base path on the remote FTP filesystem from which to grab the resource 


public class FTPSource implements ContentSource
{
    String server;
    public void setServer(String server){this.server = server;}

    String username;
    public void setUsername(String username){this.username = username;}

    String password;
    public void setPassword(String password){this.password = password;}
    
    String basepath = "";
    public void setBasePath(String path){this.basepath = path;}
    
    FTPClient getConnectedClient() 
    {
        FTPClient ftp = null;
        
        // if we need a real production version of this, we should do connection pooling...as if we'd ever really use FTP for high-availability...
        try { 
            ftp = new FTPClient();
            ftp.connect(server);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new RuntimeException("FTPSource: FTP connection attempt was not replied properly");
            }
            
            if (!ftp.login(username, password))
            {
                ftp.logout();
                throw new RuntimeException("FTPSource: invalid credentials");
            }
            
        } catch (SocketException se) {
            throw new RuntimeException("FTPSource: FTP connection attempt resulted in SocketException");            
        } catch (IOException ioe) {
            throw new RuntimeException("FTPSource: FTP connection attempt resulted in IOException");                        
        }
        
        return ftp;
    }
    
    
    public boolean hasContent(String contentItem, HttpServletRequest request)
    {
        String itempath = basepath+contentItem;
        
        FTPClient ftp = getConnectedClient();
        
        try { 
            String itemstatus = ftp.getStatus(itempath);
            String modtime = ftp.getModificationTime(itempath);
            String replystring = ftp.getReplyString();
            //FTPFile[] filelist = ftp.listFiles(itempath);
            if (modtime == null) { return false; }            
        } catch (IOException ioe) {
            throw new RuntimeException("FTPSource: FTP exists check resulted in IOException");                                                
        } finally {
            if (ftp.isConnected()) {
                try{ftp.disconnect();}catch(IOException ioe){};
            }
        }
        return true;
    }
    

    public ContentResponse getContent(String contentItem, HttpServletRequest req)
    {
        String itempath = basepath+contentItem;
        
        FTPClient ftp = getConnectedClient();
        
        try {
            
            // why not ftp.setFileTransferType()??
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ftp.retrieveFile(itempath, baos);
            
            String replystring = ftp.getReplyString();
            
            //resp.setContentType("text/html");
            // convert BAOS to inputstream
            ByteArrayInputStream inputstream = new ByteArrayInputStream(baos.toByteArray());
            
            ContentResponse response = new ContentResponse();
            response.setContent(inputstream);
            
            ftp.logout();
            
            return response;
            
        } catch (IOException ioe) {
            throw new RuntimeException("FTPSource: FTP retrieval attempt resulted in IOException");                                                
        } finally {
            if (ftp.isConnected()) {
                try{ftp.disconnect();}catch(IOException ioe){};
            }
        }
        
        
        
        
    }
    

}
