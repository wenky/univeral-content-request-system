package com.uhg.ovations.portal.partd.simplewebservice;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleRequestGenerationException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceException;

public class SimpleWebServiceUUIDConstantsImpl  extends SimpleWebServiceImpl
{
	private static final Logger log = Logger.getLogger(SimpleWebServiceUUIDConstantsImpl.class);
	
	boolean generateMessageUUID = true;
	Map<String,String> constants = null;
	
	public Object callWebservice(String... parameters)
	{
		String[] merged = null;
		try {
			if (generateMessageUUID) {			
				merged = SimpleWSUtils.appendStrings(parameters,constants,"messageUUID",UUID.randomUUID().toString());
			} else {
				merged = SimpleWSUtils.appendStrings(parameters,constants);
			}
		} catch (SimpleServiceException sse) {
			throw sse; // don't repackage
		} catch (Exception e) {
			throw new SimpleRequestGenerationException("Error merging invocation parameters, constants, and UUID into unified subsititution parameter list",e);
		}
		return super.callWebservice(merged);
	}

	
	public Map<String, String> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, String> constants) {
		this.constants = constants;
	}

	public boolean isGenerateMessageUUID() {
		return generateMessageUUID;
	}

	public void setGenerateMessageUUID(boolean generateMessageUUID) {
		this.generateMessageUUID = generateMessageUUID;
	}

	
	
}
