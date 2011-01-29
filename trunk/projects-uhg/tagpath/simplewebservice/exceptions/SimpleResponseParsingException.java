package com.uhg.ovations.portal.partd.simplewebservice.exceptions;

// something went wrong with parsing the response
public class SimpleResponseParsingException extends SimpleServiceException 
{
    public SimpleResponseParsingException() {super();}	
    public SimpleResponseParsingException(String message) {super(message);}
    public SimpleResponseParsingException(String message, Throwable cause) {super(message, cause);}
    public SimpleResponseParsingException(Throwable cause) {super(cause);}	
}
