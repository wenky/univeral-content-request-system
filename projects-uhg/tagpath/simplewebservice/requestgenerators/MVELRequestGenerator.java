package com.uhg.ovations.portal.partd.simplewebservice.requestgenerators;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mvel.TemplateInterpreter;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleRequestGenerator;
import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleRequestGenerationException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceException;

public class MVELRequestGenerator implements SimpleRequestGenerator 
{
	private static Logger log = Logger.getLogger(MVELRequestGenerator.class);
    String requestTemplatePath = null;
    
    transient String requestTemplateText = null;

	public String createRequest(String... parameters) 
	{
		try {
			Map environment = SimpleWSUtils.mvelAddToContextEscapeXML(new HashMap(),parameters);			
			String requestXML = mvelSubstitute(environment);
			if (log.isDebugEnabled())log.debug("Generated XML message: "+requestXML);
			return requestXML;
		} catch(SimpleServiceException sse) {
			// rethrow
			throw (sse);
		} catch (Exception e) {
			// repackage
			throw new SimpleRequestGenerationException("Error generating request body",e);
		}
	}
	
    String mvelSubstitute(Map context)
    {
    	if (requestTemplateText == null) {
    		initRequestTemplate();
    	}
    	
        // execute the template
        String xml = (String) TemplateInterpreter.eval(requestTemplateText, context);
        
        return xml;
    }
        
    void initRequestTemplate()
    {
        try { 
            InputStream iostream = MVELRequestGenerator.class.getResourceAsStream(getRequestTemplatePath());
            String templateText = IOUtils.toString(iostream);
            requestTemplateText = templateText;
        } catch (IOException ioe) {
            throw new SimpleRequestGenerationException("MVELRequestGenerator unable to load request template "+getRequestTemplatePath(),ioe);
        }        
    }
	

	public String getRequestTemplatePath() {
		return requestTemplatePath;
	}

	public void setRequestTemplatePath(String requestTemplatePath) {
		this.requestTemplatePath = requestTemplatePath;
	}
	
	

}
