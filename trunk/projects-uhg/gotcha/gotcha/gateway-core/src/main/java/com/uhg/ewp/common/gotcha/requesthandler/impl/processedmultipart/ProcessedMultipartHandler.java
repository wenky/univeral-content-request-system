package com.uhg.ewp.common.gotcha.requesthandler.impl.processedmultipart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.requesthandler.idef.ContentRequestHandler;
import com.uhg.ewp.common.gotcha.requesthandler.responseprocessor.idef.ContentResponseProcessor;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class ProcessedMultipartHandler implements ContentRequestHandler
{
    // PROPERTIES ----    
    ContentRequestParser        requestParser = null;    
    ContentSource               contentSource = null;    
    List<ContentResponseProcessor>      responseProcessors = null;
    // ---- END PROPS


 



    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        ContentRequest contentrequest = requestParser.parseRequest(req);
        
        if (!contentrequest.isParsedSuccessfully()) {
            return false;
        } else {
            
            // get security group membership info from parsed content request
        
            ContentResponse contentresponse = contentSource.getContent(contentrequest);
            
            if (contentresponse.isFound()) {
                if (!contentresponse.isMultipart()) {
                    try { 
                        IOUtils.copy(contentresponse.getContent(), resp.getOutputStream());
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException writing content response to HTTP Response",ioe);                
                    }
                    return true;
                } else {
                    try {
                        if (responseProcessors != null) {
                            for (ContentResponseProcessor processor : responseProcessors)
                            {
                                processor.processResponse(contentrequest, contentresponse);
                            }
                        }
                                                
                        OutputStream out = resp.getOutputStream();
                        for (ContentResponse part : contentresponse.getMultipartResponse())
                        {
                            IOUtils.copy(part.getContent(), out);                            
                        }
                        
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException writing content response to HTTP Response",ioe);                
                    }
                    return true;
                }
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

    public List<ContentResponseProcessor> getResponseProcessors()
    {
        return responseProcessors;
    }

    public void setResponseProcessors(List<ContentResponseProcessor> responseProcessors)
    {
        this.responseProcessors = responseProcessors;
    }

}
