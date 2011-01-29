package com.uhg.ovations.portal.partd.simplewebservice.exceptions;

// base/simplest exception for "something went wrong with the service call"

public class SimpleServiceException extends RuntimeException 
{
    public SimpleServiceException() {super();}	
    public SimpleServiceException(String message) {super(message);}
    public SimpleServiceException(String message, Throwable cause) {super(message, cause);}
    public SimpleServiceException(Throwable cause) {super(cause);}	
}
