package com.uhg.ewp.common.gotcha.contentsource.impl.filesystem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceConfigurationException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.ActivationMimeTypes;
import com.uhg.ewp.common.gotcha.util.log.Lg;

// gets resource/content item relative to a configured path on the local filesystem
// - should be usable against Vignette Deployment Agent-placed content 

public class FileSystemSource implements ContentSource
{
    // ---- PROPERTIES

    // set
    String baseroot;
    
    //getset
    String sourceName = "FileSystem";
    

    // ---- END PROPERTIES

    public ContentResponse getContent(ContentRequest contentItem)
    {
        
        BaseContentResponse response = new BaseContentResponse();
        response.setItemRef(contentItem.getContentItem());
        response.setSourceName(getSourceName());

        // form filename from baseroot
        String filepath = buildFilePath(contentItem);
        response.setSourceId(filepath);
        File thefile = getFile(filepath);
        if (!thefile.exists())
        {
            response.setFound(false);
            return response;
        }
        else if (!thefile.isFile())
        {
            response.setFound(false);
            return response;
        }
        else
        {
            try
            {
                // it was found...
                response.setFound(true);
                
                // size
                long size = thefile.length();
                response.setSize(size);

                // determine mimetype, ?default to html?
                String mimetype = ActivationMimeTypes.determineMimeType(thefile.getName());
                response.setMimetype(mimetype);

                // lastmodified is the version
                Long lastmod = thefile.lastModified();
                response.setLastModified(lastmod);

                // set content buffer
                BufferedInputStream fis = new BufferedInputStream(new FileInputStream(thefile));
                response.setContent(fis);
                
                return response;

            }
            catch (IOException ioe)
            {
                throw new SourceRetrievalException("FSSRC-IOERR",Lg.err("IO Error reading file"+filepath,ioe), ioe);
            }

        }

    }
    
    
    // ---- overrideable logic
    protected String buildFilePath(ContentRequest contentReq)
    {
        if (baseroot == null) throw new SourceConfigurationException("FSSRC-CFG-BASE",Lg.err("FileSystemSource baseroot not configured"));

        String filepath = baseroot + contentReq.getContentItem();
        return filepath;
    }
    
    protected File getFile(String filepath)
    {
        File thefile = new File(filepath);
        return thefile;
    }

    // ---- getter setter cruft

    public void setBaseroot(String baseroot)
    {
        this.baseroot = baseroot;
    }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }    

}
