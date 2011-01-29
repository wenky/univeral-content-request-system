package com.uhg.ovations.portal.partd.simplewebservice;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleRequestGenerationException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceInvocationException;

public class SimpleWebServiceImpl implements SimpleWebService 
{
	private static final Logger log = Logger.getLogger(SimpleWebServiceImpl.class);
	
	String requestURI = null;
	String contentType = null;

	// this singleton MUST BE THREADSAFE (set connection manager to MultiThreadedHttpConnectionManager)
	HttpClient httpClient = null;
	
	// this singleton MUST BE THREADSAFE
	SimpleRequestGenerator requestGenerator = null;
	
	// this singleton MUST BE THREADSAFE
	SimpleResponseParser responseParser = null;
	
	public Object callWebservice(String... parameters)
	{
		String requestXML = null;
		try {
			requestXML = getRequestGenerator().createRequest(parameters);
		} catch (SimpleServiceException sse) {
			throw sse;
		} catch (Exception e) {
			if (log.isDebugEnabled())log.debug("Request Generation unpackaged exception "+e.getMessage(),e);
			throw new SimpleRequestGenerationException("Error generating request in simple service "+requestURI,e);
		}

		InputStream response = null;
		HttpMethod method = null;
		try {
			method = SimpleWSUtils.makePostCall(requestXML,httpClient,requestURI,contentType);
            response = method.getResponseBodyAsStream();
        } catch (SimpleServiceException sse) {
			throw sse;
		} catch (Exception e) {
			if (log.isDebugEnabled())log.debug("Request Transmission unpackaged exception "+e.getMessage(),e);
			throw new SimpleServiceInvocationException("Error invoking service in simple service "+requestURI,e);
		}
		
		Object responseObject = null;
		try {
			responseObject = getResponseParser().parseResponse(response);
		} catch (SimpleServiceException sse) {
			throw sse;
		} catch (Exception e) {
			if (log.isDebugEnabled())log.debug("Response Parsing unpackaged exception "+e.getMessage(),e);
			throw new SimpleResponseParsingException("Error parsing response in simple service "+requestURI,e);
		} finally {
			try {response.close(); } catch (Exception e) {log.warn("Error closing simpleWS response stream",e);}
			try {method.releaseConnection(); } catch (Exception e) {log.warn("Error closing simpleWS http connection",e);}
		}
		
		return responseObject;
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public SimpleRequestGenerator getRequestGenerator() {
		return requestGenerator;
	}

	public void setRequestGenerator(SimpleRequestGenerator requestGenerator) {
		this.requestGenerator = requestGenerator;
	}

	public SimpleResponseParser getResponseParser() {
		return responseParser;
	}

	public void setResponseParser(SimpleResponseParser responseParser) {
		this.responseParser = responseParser;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
    public void destroy() throws Exception 
    {
    	// attempt cleanup/release
    	try {
	        HttpConnectionManager connectionManager = getHttpClient().getHttpConnectionManager();
	        if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
	            ((MultiThreadedHttpConnectionManager) connectionManager).shutdown();
	        }
    	} catch (Exception e) {
    		log.error("Error while cleaning up connection manager",e);
    	}
    }
	
}
