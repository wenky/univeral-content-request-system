package com.uhg.ewp.common.gotcha.util.exception;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Exceptions fulfill the following purposes:
 * - communicating errors in recoverable and controlled means in program execution
 * - categorizing and communicating errors to code clients (either in meatspace or another system)
 * - communicating bugs and errors to diagnosing programmers
 * 
 * 
 * GotchaException attempts to formalize the following ideas about exceptions:
 *
 * - ERRORID: a new exception should create a unique id string for the specific instance of 
 * exception that has occured, and if the exception is logged, that number should be automatically
 * placed in the log to make it easy to search the log for the error
 * 
 * - ERRORCODE: a specific code location should create an exception with an error code so that the
 * categorization of exception instances in the logs can be more easily parsed
 * 
 * - TIME: the exception should record the time when it was created
 * 
 * - PRESERVE SOURCE CLASS AND METHOD: It should technically be in the call stack, but for ease
 * we should autodetect and record the class and triggering method
 * 
 * - (TODO)BETTER STACK TRACE OUTPUT: output of an exception should do this:
 *    - print a "stack" of the chained exception messages (backwards from source to end or other way?)
 *    - print the root stacktrace
 *    - print the top stacktrace
 *    
 * - (TODO) PRESERVE ROOT CAUSE: commons ExceptionUtils provides a true get root cause, but we can also
 * try to mitigate the impact of catch and throw, such as recording if an exception trace has 
 * already been logged, and avoiding another link in the chain series if catch-and-throw occurs
 * 
 * It would be really nice if there was some way in java to do message construction for the 
 * exception that constrains exceptions thrown in the process of resolving variables to compose
 * the message without lots of try-catch, such as the Elvis Operator in groovy for nullsafe refs...
 *
 * 
 * 
 * Note: creating Checked and Unchecked versions of the same code really highlights the problems
 * inherent in single-inheritance. I suppose Java could do a marker interface to distinguish the 
 * difference between Runtime and Checked, but really this is screaming for a mixin, which I may
 * need to do a crude version of to avoid DRY.
 * 
 * 
 * 
 * 
 * @author cmuell7
 *
 */


public class GotchaRuntimeException extends RuntimeException
{    
    // autoinitializing properties
    
    long creationTime = new Date().getTime();
    public long getCreationTime() { return creationTime; }
    
    String exceptionId = UUID.randomUUID().toString()+ '@' + Long.toHexString(creationTime);
    public String getExceptionId() { return exceptionId; }
    
    String errorCode = "GotchaErr_Unknown"; // all normal constructors require this...
    public String getErrorCode() { return errorCode; }
    
    String sourceClass = getCurrentThreadClassNameInternal();
    String sourceMethod = getCurrentThreadMethodNameInternal();
    int sourceLineNumber = getCurrentThreadLineNumberInternal();
    public String getSourceClass() { return sourceClass; }
    public String getSourceMethod() { return sourceMethod; }
    public int getSourceLineNumber() { return sourceLineNumber; }
    
    
    
    // constructors

    public GotchaRuntimeException(String errcode) 
    {
        super();
        errorCode = errcode;
    }

    public GotchaRuntimeException(String errcode, String message) 
    {
        super(message);
        errorCode = errcode;
    }

    public GotchaRuntimeException(String errcode, Throwable t) 
    {
        super(t);
        errorCode = errcode;
    }

    public GotchaRuntimeException(String errcode, String message, Throwable t) 
    {
        super(message,t);
        errorCode = errcode;
    }
    
    
    
    // prettier output
    
    public String getMessage()
    {
        String detailMsg = super.getMessage();
        
        return "id["+exceptionId+"] code["+errorCode+"] "+detailMsg+" in ["+sourceClass+"::"+sourceMethod+"] on ["+sourceLineNumber+"]";
    }
    
    public String getDetailMessage()
    {
        String detailMessage = super.getMessage();
        return detailMessage;
    }
    
    public List<String> getMessageStack()
    {        
        List<String> messages = new ArrayList<String>();
        Throwable currentT = this;
        while (currentT != null)
        {
            messages.add(currentT.getMessage());
        }
        return messages;
    }
    
    
    
    
    // autodetection of generating class, method, linenumber
    
    private static String getCurrentThreadMethodNameInternal() {
        try { 
            String methname = Thread.currentThread().getStackTrace()[3].getMethodName();
            return methname;
        } catch (Throwable t) {
            return "undetectable methodname";
        }
    }
    
    private static String getCurrentThreadClassNameInternal() {
        try {
            String classname = Thread.currentThread().getStackTrace()[3].getClassName();
            return classname;
        } catch (Throwable t) {
            return "undetectable classname";
        }
    }

    private static int getCurrentThreadLineNumberInternal() {
        try {
            int linenum = Thread.currentThread().getStackTrace()[3].getLineNumber();
            return linenum;
        } catch (Throwable t) {
            return -666; // return something ridiculous
        }
    }

    
}
