package com.uhg.ewp.common.gotcha.contentsource.impl.jcifs;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.ActivationMimeTypes;
import com.uhg.ewp.common.gotcha.util.log.Lg;

/**
 * 
 * use the jcifs library to do lookups on CIFS file shares (aka windows file shares)
 * 
 * smb://angus.foo.net/d/jcifs/pipes.doc  is valid
 * 
 * smb://Administrator:P%40ss@msmith1/c/WINDOWS/Desktop/foo.txt  note the URL encoding of %40 aka '@' 
 * 
 * smb://domain;username:password@server/share/path/to/file.txt  is the basic syntax...
 * 
 * @author cmuell7
 *
 */

public class CIFSSmbFileSource implements ContentSource
{
    
    // ---- PROPERTIES

    // set
    String baseroot;
    
    // set
    String sourceName = "CIFS-SMB";

    // ---- END PROPERTIES




    public ContentResponse getContent(ContentRequest contentItem)
    {
        BaseContentResponse response = new BaseContentResponse();

        // form filename from baseroot
        String filepath = buildFilePath(contentItem);
        response.setSourceId(filepath);
        response.setItemRef(contentItem.getContentItem());
        response.setSourceName(getSourceName());
        

        SmbFile smbfile = null;
        
        try { 
            smbfile = new SmbFile(filepath);
        } catch (MalformedURLException male) {
            Lg.err("Bad URL for CIFS path "+filepath,male); 
            throw new SourceRetrievalException("CIFSSMBFILESRC-BADURL","Bad URL for CIFS path",male);            
        }
        
        InputStream inputstream = null;
        try {
            inputstream = smbfile.getInputStream();
        } catch (IOException ioe) {
            Lg.err("IO Error getting inputstream for path "+filepath,ioe); 
            throw new SourceRetrievalException("CIFSSMBFILESRC-GETSTREAMFAIL","IO Error getting inputstream for CIFS path",ioe);
        }
        
        if (inputstream == null) {
            //because user/pass may be embedded in filepath, this doesn't go in the err msg
            response.setFound(false);
            return response;
        }
        
        try { 
            // size
            long size = smbfile.length();
            response.setSize(size);
        } catch (SmbException smbe) {
            Lg.err("SMB Exception getting file length for path "+filepath,smbe); 
            throw new SourceRetrievalException("CIFSSMBFILESRC-GETLENGTHFAIL","SMB Exception getting file length for path",smbe);
        }

        // determine mimetype, ?default to html?
        String mimetype = ActivationMimeTypes.determineMimeType(smbfile.getName());
        response.setMimetype(mimetype);

        // lastmodified is the version
        try {
            Long lastmod = smbfile.lastModified();
            response.setLastModified(lastmod);
        } catch (SmbException smbe) {
            Lg.err("SMB Exception getting last modified for path "+filepath,smbe); 
            throw new SourceRetrievalException("CIFSSMBFILESRC-GETLASTMODFAIL","SMB Exception getting last modified for path ",smbe);
        }

        // set content buffer
        response.setFound(true);
        response.setContent(inputstream);
        
        return response;        
    }

    
    // ---- overrideable logic
    protected String buildFilePath(ContentRequest contentReq)
    {
        String filepath = baseroot + contentReq.getContentItem();
        return filepath;
    }

    
    // ---- getter setter cruft

    public void setBaseroot(String baseroot)
    {
        this.baseroot = baseroot;
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
