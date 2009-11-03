package com.uhg.umvs.bene.cms.contentretrieval.contentsource;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;

public class LocalHTTPSource implements ContentSource
{
    // simple pass-thru (for example, if there are two different versions of jackrabbits, we can't access them in the same jvm
    // - assumes local "hard relative"
    String localWebappName = null;
    public void setLocalWebappName(String localWebappName) {this.localWebappName = localWebappName;}

    public void getContent(String contentItem, HttpServletRequest request, HttpServletResponse resp)
    {
        // construct new url
        String origurl = request.getRequestURL().toString();
        //String appname = request.getServletPath();
        //String requri = request.getRequestURI();
        // protocol "://" server ":" port (contextpath servletpath pathinfo) or (requestURI) "?" querystring
        String querystring = request.getQueryString();        
        String webappname = request.getContextPath();
        String newurl = origurl.replace(webappname, localWebappName);
        if (!StringUtils.isEmpty(querystring)) { 
            newurl += '?' + querystring;
        }
        
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(newurl);

        try {
            try {
                client.executeMethod(method);
            } catch (HttpException he) {
                throw new RuntimeException("LocalHTTPSource: http error on url "+newurl,he);
            }
            //String htmlpage = method.getResponseBodyAsString();            
            //resp.getOutputStream().print(htmlpage);
            //resp.getOutputStream().flush();
            int read = 0;
            byte[] bytes = new byte[1024];
       
            //While there are still bytes in the file, read them and write them to our OutputStream
            InputStream contentstream = method.getResponseBodyAsStream();
            OutputStream os = resp.getOutputStream();
            while((read = contentstream.read(bytes)) != -1) {
               os.write(bytes,0,read);
            }
            
        } catch (IOException ioe) {
            throw new RuntimeException("LocalHTTPSource: IO Error "+newurl,ioe);
        }
    }

}
