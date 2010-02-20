package com.uhg.ewp.common.gotcha.requesthandler.impl.simple;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.requesthandler.idef.ContentRequestHandler;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;

public class SimpleRequestHandler implements ContentRequestHandler
{
    // PROPERTIES ----
    
    /* set */
    ContentRequestParser requestParser = null;
    
    /* set */
    ContentSource contentSource = null;
    
    // ---- END PROPS


    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        ContentRequest contentrequest = requestParser.parseRequest(req);
        
        if (!contentrequest.isParsedSuccessfully()) {
            return false;
        } else {
        
            ContentResponse contentresponse = contentSource.getContent(contentrequest);
            
            if (contentresponse.isFound()) {
                try { 
                    IOUtils.copy(contentresponse.getContent(), resp.getOutputStream());
                } catch (IOException ioe) {
                    throw new RuntimeException("IOException writing content response to HTTP Response",ioe);                
                }
                return true;
            } else {
                return false;
            }
        }
        
    }
    

    // getters setters
    public void setRequestParser(ContentRequestParser requestParser)
    {
        this.requestParser = requestParser;
    }

    public void setContentSource(ContentSource contentSource)
    {
        this.contentSource = contentSource;
    }
    
    
    
    
}
