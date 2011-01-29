package com.uhg.ovations.portal.partd.simplewebservice;

// delegate objects whose job is to create the request XML
public interface SimpleRequestGenerator {
	String createRequest(String... parameters);
}
