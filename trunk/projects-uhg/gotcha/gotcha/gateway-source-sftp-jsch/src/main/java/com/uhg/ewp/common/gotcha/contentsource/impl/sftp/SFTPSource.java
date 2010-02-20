package com.uhg.ewp.common.gotcha.contentsource.impl.sftp;

import java.io.InputStream;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;
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

public class SFTPSource implements ContentSource
{
    // ---- PROPERTIES

    // set
    String server = null;

    // set
    String username = null;

    // set
    String password = null;

    // set
    String basepath = "";
    
    // set
    String sourceName = "SFTP";

    // ---- END PROPERTIES


    ChannelSftp getConnectedClient()
    {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpch = null;

        try
        {

            session = jsch.getSession(username, server, 22);
            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();

            return (ChannelSftp) channel;

        }
        catch (JSchException jsche)
        {
            throw new SourceAccessException("SFTPSRC-CONNECTFAIL",Lg.err("jsch exception", jsche),jsche);
        }
    }

    public ContentResponse getContent(ContentRequest req)
    {
        BaseContentResponse response = new BaseContentResponse();

        String itempath = basepath + req.getContentItem();

        ChannelSftp sftp = null;

        try
        {
            sftp = getConnectedClient();
            
            //Vector basefilelist = sftp.ls("/");

            String itemdir = "/";
            String filename = "";
            if (itempath.contains("/"))
            {
                itemdir = itempath.substring(0, itempath.lastIndexOf('/'));
                filename = itempath.substring(itempath.lastIndexOf('/') + 1);
                if ("".equals(itemdir))
                {
                    itemdir = "/";
                }
            }

            sftp.cd(itemdir);

            Vector filelist = sftp.ls(itemdir);

            LsEntry match = null;

            for (int i = 0; i < filelist.size(); i++)
            {
                LsEntry lse = (LsEntry) filelist.get(i);
                if (lse.getFilename().equals(filename))
                {
                    match = lse;
                    break;
                }
            }

            if (match == null)
            {
                // not found...
                response.setFound(false);
                return response;
            }

            response.setFound(true);

            String mimetype = ActivationMimeTypes.determineMimeType(filename);
            response.setMimetype(mimetype);

            SftpATTRS attrs = match.getAttrs();
            int mtime = attrs.getMTime();
            response.setLastModified((long)mtime);
            
            long size = attrs.getSize();
            response.setSize(size);

            InputStream stream = sftp.get(itempath);

            response.setContent(stream);
            response.setItemRef(req.getContentItem());
            response.setSourceId(itempath);
            response.setSourceName(getSourceName());

            return response;
        }
        catch (SftpException sftpe)
        { 
            if ("No such file".equals(sftpe.getMessage())) {
                response.setFound(false);
                return response;
            }
            throw new SourceRetrievalException("SFTPSRC-GETFILE-FAIL",Lg.err("sftp exception on retrieval of item "+itempath, sftpe),sftpe);
        }
        finally
        {
            try { sftp.quit(); } catch (Exception e) {Lg.trc("SFTP quit cleanup failed");}
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
