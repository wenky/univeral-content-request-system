package com.uhg.umvs.bene.cms.contentretrieval.contentsource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;


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
    
    

    public void getContent(String contentItem, HttpServletRequest req, HttpServletResponse resp)
    {
        String itempath = basepath+contentItem;
        
        FTPClient ftp = null;
        
        try { 
            ftp = new FTPClient();
            ftp.connect(server);

            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new RuntimeException("FTPSource: connection attempt was not replied properly");
            }
        } catch (SocketException se) {
            throw new RuntimeException("FTPSource: connection attempt resulted in SocketException");            
        } catch (IOException ioe) {
            throw new RuntimeException("FTPSource: connection attempt resulted in IOException");                        
        }
        
        
        try {
        
            if (!ftp.login(username, password))
            {
                ftp.logout();
                throw new RuntimeException("FTPSource: invalid credentials");
            }
            
            // why not ftp.setFileTransferType()??
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ftp.retrieveFile(itempath, baos);
            
            String replystring = ftp.getReplyString();
            
            //resp.setContentType("text/html");
            OutputStream output = resp.getOutputStream();
            baos.writeTo(output);
            output.flush();
            baos.close();
            ftp.logout();
            
        } catch (IOException ioe) {
            throw new RuntimeException("FTPSource: FTP retrieval attempt resulted in IOException");                                                
        } finally {
            if (ftp.isConnected()) {
                try{ftp.disconnect();}catch(IOException ioe){};
            }
        }
        
        
        
        
    }
    

}
