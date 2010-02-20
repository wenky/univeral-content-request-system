package com.uhg.ewp.common.gotcha.requesthandler.impl.multipart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.requesthandler.idef.ContentRequestHandler;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;

public class SimpleMultipartHandler implements ContentRequestHandler
{

    // PROPERTIES ----    
    ContentRequestParser        requestParser = null;    
    ContentSource               contentSource = null;
    // ---- END PROPS


    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        ContentRequest contentrequest = requestParser.parseRequest(req);
        
        if (!contentrequest.isParsedSuccessfully()) {
            return false;
        } else {
        
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
                        OutputStream out = resp.getOutputStream();
                        List<ContentResponse> multipart = contentresponse.getMultipartResponse();
                        for (ContentResponse part : multipart) 
                        {
                            //for (Object attr : part.getMetaData().keySet())
                            //{
                            //    String attrname = (String)attr;
                            //    String attrval = part.getMetaData().get(attrname).toString();
                            //    IOUtils.write(("attrname: "+attrname+" attrval: "+attrval+"<BR>").getBytes(),out);
                            //    
                            //}
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
}


