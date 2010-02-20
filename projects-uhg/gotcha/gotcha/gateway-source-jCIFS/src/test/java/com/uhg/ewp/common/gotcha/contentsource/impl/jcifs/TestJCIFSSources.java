package com.uhg.ewp.common.gotcha.contentsource.impl.jcifs;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentRequest;

public class TestJCIFSSources
{
    public static void main(String[] args) throws Exception
    {
        TestCIFSSmbFileSource();
    }
    
    
    public static void TestCIFSSmbFileSource() throws Exception
    {
        
        CIFSSmbFileSource cifssrc = new CIFSSmbFileSource();
        cifssrc.baseroot = "smb://MS;testusername:testpassword@msp09fil03/homedirs/cmuell7/";
        
        BaseContentRequest req = new BaseContentRequest();
        
        req.setContentItem("CIFStest.html");
        
        ContentResponse resp = cifssrc.getContent(req);
        
        String content = IOUtils.toString(resp.getContent());
        
        System.out.println(content);
    }

}
