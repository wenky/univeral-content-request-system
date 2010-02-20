package com.uhg.ewp.common.gotcha.contentsource.impl.webdavsardine;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentRequest;

public class TestSardine
{
    public static void main(String[] args) throws Exception
    {
        
        
        WebDAVSource src = new WebDAVSource();
        src.setBaseURL("http://localhost:56008/test-webdav/");
        
        BaseContentRequest req = new BaseContentRequest();
        
        req.setContentItem("test.html");
        
        ContentResponse resp = src.getContent(req);
        
        String content = IOUtils.toString(resp.getContent());
        
        System.out.println(content);
        
    }
}
