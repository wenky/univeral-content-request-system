package com.uhg.ewp.common.gotcha.contentsource.impl.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceAccessException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.ActivationMimeTypes;
import com.uhg.ewp.common.gotcha.util.log.Lg;

// get content from an FTP server. need to configure a server. optionally provide username, password, 
// and base path on the remote FTP filesystem from which to grab the resource 

public class FTPSource implements ContentSource
{
    // ---- PROPERTIES

    // set
    String server = null;

    // set
    String username = null;

    // set
    String password = null;

    // set
    String basepath = "/";
    
    //getset
    String sourceName = "FTP";

    // ---- END PROPERTIES


    FTPClient getFTPClient()
    {
        return new FTPClient();
    }

    // TODO: caching/pooling
    FTPClient getConnectedClient()
    {
        FTPClient ftp = null;

        try
        {
            ftp = getFTPClient();
            ftp.connect(server);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                throw new SourceAccessException("FTPSRC-CONNECT-BADREPLY",Lg.err("FTP connection attempt was not replied properly"));
            }

            if (!ftp.login(username, password))
            {
                throw new SourceAccessException("FTPSRC-CONNECT-LOGINFAIL",Lg.err("FTP connection attempt failed due to invalid credentials"));
            }

        } catch (SocketException se) {
            throw new SourceRetrievalException("FTPSRC-CONNECT-SOCKETFAIL",Lg.err("FTP connection attempt resulted in SocketException",se),se);
        } catch (IOException ioe) {
            throw new SourceRetrievalException("FTPSRC-CONNECT-IOFAIL",Lg.err("FTP connection attempt resulted in IOException",ioe),ioe);
        } finally {
            try {ftp.disconnect();} catch (Exception e) {Lg.trc("FTP cleanup disconnect failed");}
        }

        return ftp;
    }

    public ContentResponse getContent(ContentRequest req)
    {
        BaseContentResponse response = new BaseContentResponse();
        response.setItemRef(req.getContentItem());
        response.setSourceName(getSourceName());

        String itempath = basepath + req.getContentItem();
        response.setSourceId(itempath);

        FTPClient ftp = getConnectedClient();

        try
        {

            // String itemstatus = ftp.getStatus(itempath);
            // String replystring = ftp.getReplyString();
            // FTPFile[] filelist = ftp.listFiles(itempath);

            String modtime = ftp.getModificationTime(itempath);
            if (modtime == null)
            {
                response.setFound(false);
                return response;
            }
            
                        
            int mdtm = ftp.mdtm(itempath);
            response.setFound(true);
            response.setLastModified((long)mdtm);
            
            // guess mimetype
            String mimetype = ActivationMimeTypes.determineMimeType(itempath);
            response.setMimetype(mimetype);

            // why not ftp.setFileTransferType()??
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ftp.retrieveFile(itempath, baos);
            byte[] content = baos.toByteArray();
            long size = content.length;
            response.setSize(size);
            ByteArrayInputStream inputstream = new ByteArrayInputStream(content);
            
            response.setContent(inputstream);

            ftp.logout();

            return response;

        }
        catch (IOException ioe)
        {
            throw new SourceRetrievalException("FTPSRC-GETFAIL",Lg.err("FTP retrieval attempt resulted in IOException",ioe),ioe);
        }
        finally
        {
            if (ftp.isConnected()) try { ftp.disconnect(); }catch (IOException ioe){Lg.trc("FTP disconnect cleanup failed");}
        }
    }

    // ---- getter setter cruft

    public void setServer(String server)
    {
        this.server = server;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setBasepath(String path)
    {
        this.basepath = path;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

}
