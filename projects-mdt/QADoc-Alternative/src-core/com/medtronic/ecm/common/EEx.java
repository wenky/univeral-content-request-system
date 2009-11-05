package com.medtronic.ecm.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extensible and adaptable class for EnrichableExceptions, which is an alternative method
 * for tracking exception passing through applications using stacks of error codes. It refines the
 * concept of wrapping disparate exceptions encountered in applications inside a generalized 
 * application-specific class. Normally this is done via exception chaining, but simply using 
 * chained exceptions tends to result in too many stack traces polluting logs, when the original 
 * exception stack trace is all that is desired. Essentially, this is a model of decorating 
 * exceptions rather than chaining them.
 * 
 * Goals of this strategy:
 * - preserve original stack trace and exception
 * - prevent the original stack trace from getting wrapped/chained unnecessarily
 * - use error codes for quick back-references to the code
 * - organize and concentrate error messages, and chain error messages rather than exceptions 
 * 
 * IMPORTANT: create EnrichableException instances by calling the EnrichableException.create()
 * factory method. This was implemented as a convenience for when you do:
 * 
 * try {
 *     misbehavingCode();
 * } catch (Exception e) {
 *     throw EnrichableException.create("EC","Errmsg",e);
 * }
 * 
 * If the incoming exception e is an EnrichableException, doing throw new EnrichableException(e)
 * would essentially rewrap the previous EnrichableException, which we explictly don't want to do, 
 * remember we want to preserve the original excpetion and stack trace. With the factory method,
 * we can first check instanceof the chained exception, and then only addInfo if the chained 
 * exception is already an EnrichableException. Unfortunately, the restrictions on calling super()
 * for class constructors makes instanceof testing difficult. This is not a problem with a factory
 * method. You'll just have to get used to it, folks. 
 * 
 * Taken from http://tutorials.jenkov.com/java-exception-handling/exception-enrichment.html...   
 *
 *  Exception enrichment is an alternative to exception wrapping. Exception wrapping has a couple 
 *  of disadvantages that exception enrichment can fix. These disadvantages are:
 *  
 *  - Exception wrapping may result in very long stack traces consisting of one stack trace for 
 *  each exception in the wrapping hierarchy. Most often only the root stack trace is interesting.
 *  The rest of the stack traces are then just annoying.
 *  
 *  - The messages of the exceptions are spread out over the stack traces. The message of an 
 *  exception is typically printed above the stack trace. When several exceptions wrap each other 
 *  in a hierarchy, all these messages are spread out in between the stack traces. This makes it 
 *  harder to determine what went wrong, and what the program was trying to do when the error 
 *  happened. In other words, it makes it hard to determine in what context the error occurred. The
 *  error might have occurred in a PersonDao class, but was it called from a servlet or from a web 
 *  service when it failed? 
 *
 *  Here is an example. Method level1 is called, which calls level 2, then level 3, finally level 4
 *  which throws an IllegalArgumentException.
 *  As the exception passes back up to level3, level2, and level1, each catch appends/pushes/decorates
 *  the exception with an exception code and message specific to its context, and then rethrows up
 *  the stack (as is typical): 
 *  
 *  <blockquote><pre>
 *  public void level1(){
 *      try {
 *          level2();
 *      } catch (Exception e){
 *          throw EnrichableException.create("E1", "Error in level 1");
 *      }
 *  }
 *  
 *  public void level2(){
 *      try {
 *          level3();
 *      } catch (Exception e){
 *          throw EnrichableException.create("E2", "Error in level 2, throw back to level 1");
 *      }
 *  }
 *  
 *  public void level3(){
 *      try {
 *          level4();
 *      } catch(IllegalArgumentException e){
 *          // low-level exception detected, wrap and rethrow
 *          throw EnrichableException.create("E3", "Error at level 3, throw back to lvl 2",e);  
 *      }
 *  }
 *  
 *  public void level4(){
 *      // throw a low-level exception
 *      throw new IllegalArgumentException("incorrect argument passed");
 *  }
 *  </pre></blockquote>
 * 
 * If e.printStackTrace is called on the enrichable exception we get this output:
 * 
 * <blockquote><pre>
 * [level1:E1][level2:E2][level3:E3]
 * [level1:E1]Error in level 1
 * [level2:E2]Error in level 2, throw back to level 1
 * [level3:E3]Error at level 3, throw back to lvl2
 * java.lang.IllegalArgumentException: incorrect argument passed
 * 
 *  at exception.ExceptionTest$1.handle(ExceptionTest.java:8)
 *  at exception.ExceptionTest.level3(ExceptionTest.java:49)
 *  at exception.ExceptionTest.level2(ExceptionTest.java:38)
 *  at exception.ExceptionTest.level1(ExceptionTest.java:29)
 *  at exception.ExceptionTest.main(ExceptionTest.java:21)
 * Caused by: java.lang.IllegalArgumentException: incorrect argument passed
 *  at exception.ExceptionTest.level4(ExceptionTest.java:54)
 *  at exception.ExceptionTest.level3(ExceptionTest.java:47)
 *  ... 3 more
 * </pre></blockquote>
 *  
 * Since only one stack trace is embedded in the error, only one is printed. In addition, the output
 * displays an overall error code "[level1:E1][level2:E2][level3:E3]" which is a compact record of 
 * not only the original error, but where in the application it occurred and passed through (basically
 * a more compact, categorizable stack trace). Then a dump of the error messages that were attached
 * at each catch...throw level.
 * 
 * For the medtronic layer, additional features have been added:
 * - the EnrichableException constructor and addinfo can be passed a C-style printf string and a list
 * of varargs to be formatted into the message.
 * - when wrapping an exception in EnrichableException the first time, make sure to place the Throwable
 * that is being wrapped as the last argument in the method call, even if you have a formatted message
 * and arguments. The method will autodetect the throwable at the end of the arguments and use that as
 * the Throwable to wrap/chain.
 * - if the formatted message's arguments has an IDfTypedObject object in the list, it will be 
 * automatically converted to a string listing the object's id, and if applicable, name, type, and 
 * the current documentum user.
 * - the current context/method name is autodetected from the call stack when the error code and message
 * are appended. 
 * 
 * @author muellc4 (based/adapted from Jakob Jakov's article)
 * @version $Revision: 1.5 $
 *
 */

public class EEx extends RuntimeException 
{
    public static final long serialVersionUID = -1;
    
    public Map errorContext = null;

    protected List<InfoItem> infoItems = new ArrayList<InfoItem>();

    protected class InfoItem
    {
        public String errorContext = null;
        public String errorCode  = null;
        public String errorText  = null;
        public InfoItem(String contextCode, String errorCode, String errorText)
        {
            this.errorContext = contextCode;
            this.errorCode   = errorCode;
            this.errorText   = errorText;
        }
    }

    /**
     * TODO: ADD DESCRIPTION
     *  
     * @param saveLock <font color="#0000FF"><b>(boolean)</b></font> TODO:
     * @param versionLabel <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param args <font color="#0000FF"><b>(Object[])</b></font> TODO:
     * 
     * 
     * @since 1.0
     * 
     */ 
    private static String getCurrentThreadMethodNameInternalA() {
        // for direct addinfo call...
        String curclass = Thread.currentThread().getStackTrace()[4].getMethodName();
        return curclass;
    }
    
    /**
    *
    * TODO: ADD DESCRIPTION
    * 
    * 
    * @since 1.0
    *  
    */
    private static String getCurrentThreadMethodNameInternalB() {
        // for constructor call
        String curclass = Thread.currentThread().getStackTrace()[4].getMethodName();
        return curclass;
    }

    /**
     * Construct a new EnrichableException, wrapping a Throwable if one is provided at the end of 
     * the parameter list. The current method name is autodetected from the call stack, so that does
     * not need to be passed in.
     * @param  <font color="#0000FF"><b>(String)</b></font> errorCode
     *         Error Code to add/decorate to the current error code list
     * @param  <font color="#0000FF"><b>(String)</b></font>  errorMessage 
     *         Error Message to add to the error message list. This can be a C-style printf statement,
     *         and you can provide varargs that the formatter will use. If there is a DCTM typed object
     *         in the args list, it will be converted to a minidump of the object id, name, type, and 
     *         current user, if possible.  
     * @param  <font color="#0000FF"><b>(Object[])</b></font>  args 
     *         Variable-length arguments to be formatted into the errorMessage. Note that the last vararg
     *         should be the Throwable to be wrapped by this Enrichable Exception instance. 
     */    
    private EEx(String errorCode,String errorMessage, Object[] args)
    {
        super((args != null && args[args.length-1] instanceof Throwable ? (Throwable)args[args.length-1] : null));
        String errorContext = "Unknown Context";
        String formattedmsg = errorMessage;
        // scan varargs for dfc objects and change them to detailed output
        Lg.replaceObjectsWithInfo(args);
        try {errorContext = getCurrentThreadMethodNameInternalB(); } catch (Exception e) {}
        try {formattedmsg = Lg.sprintf(errorMessage, args); } catch (Exception e) {}
        addInfo(errorContext, errorCode, formattedmsg);
    }
    
    
    /**
     * TODO: ADD DESCRIPTION
     *  
     * @param  errorCode <font color="#0000FF"><b>(String)</b></font> TODO:     
     * @param  errorMessage <font color="#0000FF"><b>(String)</b></font> TODO: 
     * @param  args <font color="#0000FF"><b>(Object...)</b></font>  args 
     */    
    public static EEx create(String errorCode,String errorMessage, Object... args)
    {
        if (args != null && args[args.length-1] instanceof EEx) 
        {
            EEx ee = (EEx)args[args.length-1];
            String errorContext = "Unknown Context";
            String formattedmsg = errorMessage;
            
            // scan varargs for dfc objects and change them to detailed output
            Lg.replaceObjectsWithInfo(args);
            try {errorContext = getCurrentThreadMethodNameInternalB(); } catch (Exception e) {}
            try {formattedmsg = Lg.sprintf(errorMessage, args); } catch (Exception e) {}
            ee.addInfo(errorContext, errorCode, formattedmsg);
            return ee;
        } else return new EEx(errorCode,errorMessage,args);
    }

    
    
    /**
     * TODO: ADD DESCRIPTION
     *  
     * @param  errorContext <font color="#0000FF"><b>(String)</b></font> TODO:     
     * @param  errorCode <font color="#0000FF"><b>(String)</b></font> TODO: 
     * @param  errorText <font color="#0000FF"><b>(String)</b></font> TODO: 
     */   
    EEx addInfo(String errorContext, String errorCode, String errorText)
    {
        this.infoItems.add(new InfoItem(errorContext, errorCode, errorText));
        return this;
    }

    
    
    /**
     * Append a new error code and message to the current error code/message stack. The current method 
     * name is autodetected from the call stack, so that does not need to be passed in. 
     * @param  errorCode <font color="#0000FF"><b>(String)</b></font>
     *         Error Code to add/decorate to the current error code list
     * @param  errorMessage <font color="#0000FF"><b>(String)</b></font>
     *         Error Message to add to the error message list. This can be a C-style printf statement,
     *         and you can provide varargs that the formatter will use. If there is a DCTM typed object
     *         in the args list, it will be converted to a minidump of the object id, name, type, and 
     *         current user, if possible.  
     * @param  args <font color="#0000FF"><b>(Object...)</b></font>
     *         Variable-length arguments to be formatted into the errorMessage.  
     */    
    public EEx addInfo(String errorCode, String errorMessage, Object... args)
    {
        String errorContext = "Unknown Context";
        String formattedmsg = errorMessage;
        Lg.replaceObjectsWithInfo(args);
        try {errorContext = getCurrentThreadMethodNameInternalA(); } catch (Exception e) {}
        try {formattedmsg = Lg.sprintf(errorMessage, args); } catch (Exception e) {}
        infoItems.add(new InfoItem(errorContext, errorCode, formattedmsg));
        return this;
    }

    /**
     * TODO: 
     *
     *  @return <font color="#0000FF"><b>String</b></font> - TODO: 
     */    
    public String getCode()
    {
        StringBuilder builder = new StringBuilder();

        for(int i = this.infoItems.size()-1 ; i >=0; i--){
            InfoItem info = this.infoItems.get(i);
            builder.append('[');
            builder.append(info.errorContext);
            builder.append(':');
            builder.append(info.errorCode);
            builder.append(']');
        }

        return builder.toString();
    }

    /**
     * TODO: 
     *
     *  @return <font color="#0000FF"><b>String</b></font> - TODO: 
     */  
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getCode());
        builder.append('\n');

        //append additional context information.
        for(int i = this.infoItems.size()-1 ; i >=0; i--){
            InfoItem info =
                this.infoItems.get(i);
            builder.append('[');
            builder.append(info.errorContext);
            builder.append(':');
            builder.append(info.errorCode);
            builder.append(']');
            builder.append(info.errorText);
            if(i>0) builder.append('\n');
        }

        //append root causes and text from this exception first.
        if(getMessage() != null) {
            builder.append('\n');
            if(getCause() == null){
                builder.append(getMessage());
            } else if(!getMessage().equals(getCause().toString())){
                builder.append(getMessage());
            }
        }
        appendException(builder, getCause());

        return builder.toString();
    }

    /**
     * TODO: 
     * 
     * @param  builder <font color="#0000FF"><b>(StringBuilder)</b></font>
     *         Error Code to add/decorate to the current error code list
     * @param  throwable <font color="#0000FF"><b>(Throwable)</b></font>
     *  
     */  
    private void appendException(StringBuilder builder, Throwable throwable)
    {
        if(throwable == null) return;
        appendException(builder, throwable.getCause());
        builder.append(throwable.toString());
        builder.append('\n');
    }
    
    public Map getErrorContext() {
        if (errorContext == null) {
            errorContext = new HashMap();
        }
        return errorContext;
    }
    
}