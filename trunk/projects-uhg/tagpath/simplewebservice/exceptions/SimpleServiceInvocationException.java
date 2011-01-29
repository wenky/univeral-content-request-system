package com.uhg.ovations.portal.partd.simplewebservice.exceptions;

// something went wrong with the transmission of the request or response
public class SimpleServiceInvocationException extends SimpleServiceException 
{
    public SimpleServiceInvocationException() {super();}	
    public SimpleServiceInvocationException(String message) {super(message);}
    public SimpleServiceInvocationException(String message, Throwable cause) {super(message, cause);}
    public SimpleServiceInvocationException(Throwable cause) {super(cause);}	
}
