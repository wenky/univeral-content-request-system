package com.uhg.ewp.common.gotcha.util.log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.uhg.ewp.common.gotcha.util.log.idef.ObjectLogger;



// TODO: remove/abstract lgger dependence

/**
 * Adapted version of the google code log5j library, which uses printf-style
 * message formatting and varargs, but wraps around logger rather than Logger. Provides several 
 * convenience features:
 * - if the last argument in the varargs list is a Throwable, a stack trace is outputted as well
 * - aforementioned C-style printf message formatting. Technically this is optional... 
 * - Auto-detects class and method name that logger was called from
 * - if an IDfTypedObject is in the varargs list, it is replaced with a minidump of the object which
 *   includes id, name, type, and current user, where appropriate/possible
 *   
 * lgger's six standard levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL are all supported.
 *   
 * This class generally tries as much as possible not to throw exceptions and instead return default
 * messages, so if a string format fails, it will return the string "message format error" rather 
 * than throw a formatting exception. Likewise for class detection, it will return UnknownClass if
 * it cannot autodetect the class from the current call stack. This is in keeping with logging's 
 * style with line numbers in exception stack traces and the like. 
 * 
 * Usage: 
 * <blockquote><pre>
 *  // a simple message log
 *  Lg.ftl("test (static) a simple msg");
 * 
 *  // simple message formatting 
 *  Lg.ftl("test (static) a simple substitution: %s","a-string");
 * 
 *  // simple message format + exception trace log
 *  Exception e = new Exception("BigBadError");
 *  Lg.ftl("test (static) sub+stacktrace: %d",4,e);
 *
 * </pre></blockquote>
 * 
 * Note that this class's printf styles avoids overhead of string concatenation in debug statements 
 * most of the time. For example, in normal logging, even if debug were off, this code would 
 * still perform string concatenations:
 * 
 *              logger.debug("Hi "+lastname+", "+firstname);
 * 
 * This is because java must evaluate the string concatenation into a single String object that is 
 * passed to the method. However this will not:
 * 
 *      Lg.dbg("Hi %s, %s",lastname,firstname);
 *      
 * since the formatting occurs inside the Lg.dbg method. HOWEVER this still entails overhead:
 * 
 *     Lg.dbg("Name of object: %s",sysobject.getObjectName());
 * 
 * and quite a lot since the sysobject.getObjectName() is called regardless if debug is on or not, 
 * so you still would need to do this:
 * 
 *     if (Lg.dbg())Lg.dbg("Name of object: %s",sysobject.getObjectName());
 *     
 * or rely on the minidump feature in the Lg. 
 *     
 * Yes, still not perfect...
 * 
 * @author $Author: cmuell7 $
 * 
 * @version $Revision: 1.9 $
 * 
 */
public class Lg 
{

    /**
     * Get Class from current execution stack based on stackdepth, so we don't have to burden the user to specify the class ahead of time. Cool trick I learned from google's log5j. 
     * 
     * @return <font color="#0000FF"><b>Class</b></font> - TODO:
     */
    static Class getCurrentThreadClassInternal(int stackdepth) throws ClassNotFoundException{
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String classname = stack[stackdepth].getClassName();
        return Class.forName(classname);
    }

    /**
     * Get ClassName from execution stack
     * 
     * @return <font color="#0000FF"><b>String</b></font> - TODO:
     */
    static String getCurrentThreadClassNameInternal(int stackdepth) {
        String curclass = Thread.currentThread().getStackTrace()[stackdepth].getClassName();
        return curclass;
    }
    
    /**
     * Get Method name from execution stack
     * 
     * @return <font color="#0000FF"><b>String</b></font> - TODO:
     */
    static String getCurrentThreadMethodNameInternal(int stackdepth) {
        String curclass = Thread.currentThread().getStackTrace()[stackdepth].getMethodName();
        return curclass;
    }
    
    public static boolean trc() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.TRACE);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.TRACE);}}
    public static boolean dbg() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.DEBUG);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.DEBUG);}}
    public static boolean inf() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.INFO);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.INFO);}}
    public static boolean wrn() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.WARN);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.WARN);}}
    public static boolean err() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.ERROR);} catch (Exception e) {return isLevelEnabled(Lg.class,Level.ERROR);}}
    public static boolean ftl() { try { Class curclass = getCurrentThreadClassInternal(4); return isLevelEnabled(curclass,Level.FATAL);} catch (Exception e) {return isLevelEnabled(Lg.class,Level.FATAL);}}
    

    
    /**
     * log with class and method name autodetection, intended to be called from normal code
     * 
     * the return value String is the logged message post-formatting, in case you want to reuse it 
     * for exception composition or something else...
     * 
     * @param format <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param args <font color="#0000FF"><b>(Object...)</b></font> TODO:     
     * 
     */
    public static String trc(String format, Object... args) { return log(Level.TRACE,format,args); }
    public static String dbg(String format, Object... args) { return log(Level.DEBUG,format,args); }
    public static String inf(String format, Object... args) { return log(Level.INFO,format,args); }
    public static String wrn(String format, Object... args) { return log(Level.WARN,format,args); }
    public static String err(String format, Object... args) { return log(Level.ERROR,format,args); }
    public static String ftl(String format, Object... args) { return log(Level.FATAL,format,args); }

    public static boolean trcUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.TRACE);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.TRACE);}}
    public static boolean dbgUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.DEBUG);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.DEBUG);}}
    public static boolean infUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.INFO);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.INFO);}}
    public static boolean wrnUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.WARN);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.WARN);}}
    public static boolean errUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.ERROR);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.ERROR);}}
    public static boolean ftlUtil(int stackdepth) { try { Class curclass = getCurrentThreadClassInternal(stackdepth); return isLevelEnabled(curclass,Level.FATAL);}  catch (Exception e) {return isLevelEnabled(Lg.class,Level.FATAL);}}

    public static String log(Level lvl,String format, Object... args)
    {
        // stack object detection could entail high overhead for logging...
        String curclass = "UnknownClass ";
        String methodname = "unknownmethod";
        String formattedmsg = "LOG::MSG FORMAT ERR - ["+format+']';
        try { curclass = getCurrentThreadClassNameInternal(5); } catch (Exception e) {}
        if (isLevelEnabled(curclass,lvl)) {
            replaceObjectsWithInfo(args);
            try { methodname = getCurrentThreadMethodNameInternal(5); } catch (Exception e) {}
            try { formattedmsg = sprintf(format,args); } catch (Exception e) {}
            // autodetect stack trace printout (occurs if last arg in varargs array is instance of throwable)
            if (args.length > 0 && args[args.length-1] instanceof Throwable) {
                loggit(curclass, methodname + " - " + formattedmsg, (Throwable)args[args.length-1],lvl.toString());
            } else {
                loggit(curclass, methodname + " - " + formattedmsg, null, lvl.toString());
            }
        }
        return formattedmsg;
    }    

    /**
     * same as above, but an explicit stacktrace depth can be specified, useful for utility and architecture methods, 
     * such as a log-and-throw utility class where you don't want the utility class to be visible from the stack or be
     * the autodetected classname and methodname and class that is based on logging.
     * 
     * @param format <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param args <font color="#0000FF"><b>(Object...)</b></font> TODO:     
     * 
     */
    public static String trcUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.TRACE,format,args); }
    public static String dbgUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.DEBUG,format,args); }
    public static String infUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.INFO,format,args); }
    public static String wrnUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.WARN,format,args); }
    public static String errUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.ERROR,format,args); }
    public static String ftlUtil(int stackdepth, String format, Object... args) { return logUtil(stackdepth+1,Level.FATAL,format,args); }

    public static String logUtil(int stackdepth, Level lvl, String format, Object... args)
    {
        // stack object detection could entail high overhead for logging...
        String curclass = "UnknownClass ";
        String methodname = "unknownmethod";
        String formattedmsg = "message format error";
        try { curclass = getCurrentThreadClassNameInternal(stackdepth); } catch (Exception e) {}
        if (isLevelEnabled(curclass,lvl)) {
            replaceObjectsWithInfo(args);
            try { methodname = getCurrentThreadMethodNameInternal(stackdepth); } catch (Exception e) {}
            try { formattedmsg = sprintf(format,args); } catch (Exception e) {}
            // autodetect stack trace printout (occurs if last arg in varargs array is instance of throwable)
            if (args.length > 0 && args[args.length-1] instanceof Throwable) {
                loggit(curclass, methodname + " - " + formattedmsg, (Throwable)args[args.length-1],lvl.toString());
            } else {
                loggit(curclass, methodname + " - " + formattedmsg, null, lvl.toString());                
            }
        }
        return formattedmsg;
    }    
 

    /**
     * Clone of C sprintf support. (taken from log5j)
     * 
     * @param format <font color="#0000FF"><b>(String)</b></font> TODO:
     * @param args <font color="#0000FF"><b>(Object...)</b></font> TODO:
     * @return <font color="#0000FF"><b>String</b></font> - TODO:
     * 
     * @see Formatter
     */
    public static String sprintf( String format, Object... args ) 
    {
        // a threadlocal cache was implemented in log5j for this next line code, presumably to avoid constant instantiation. May need to watch performance here...
        Formatter f = new Formatter();
        f.format( format, args );
        
        StringBuilder sb = (StringBuilder)f.out();
        String message = sb.toString();
        sb.setLength( 0 );

        return message;

    }
    
    
    public static List ObjectReplacements = new ArrayList();
    public static List ObjectReplacementContexts = new ArrayList();
    
    
    /**
     * Provide a mechanism to register handlers for special types that will expand them into more useful information in the log. For example,
     * In Documentum, we will write one for documentum objects that outputs a DfSysObject's name, id, etc.     
     *  
     * @param args <font color="#0000FF"><b>(Object[])</b></font> TODO: 
     * 
     */        
    public static void replaceObjectsWithInfo(Object[] args)
    {
        if (args != null)
        {
                for (int i=0; i < args.length; i++) 
                {
                    // indexing/mapping is complicated given object inheritance. We will have to do a linear list scan for now...
                    for (int j=0; j < ObjectReplacements.size(); j++)
                    {
                        ObjectLogger objlogger = (ObjectLogger)ObjectReplacements.get(j);
                        Class matchclass = objlogger.getMatchingClass();                        
                                if (matchclass.isInstance(args[i])) {
                                    Map context = (ObjectReplacementContexts.size() > j ? (Map)ObjectReplacementContexts.get(j) : null);
                                try {
                            String dump = objlogger.replaceObject(args[i], context);
                                        args[i] = dump;
                                } catch (Exception e) {}
                                }
                    }
                }
        }
    }    
    
    // compatibility/dependency - assume log4j for now
    
    
    static String getCategory(Object source) 
    {
        String category;
        if(source instanceof String)
            category = (String)source;
        else if(source instanceof Class)
            category = ((Class)source).getName();
        else if(source != null)
            category = source.getClass().getName();
        else
            category = "null";
        return category;        
    }
    
    static boolean isLevelEnabled(Object curclass, Level lvl) 
    {
        Logger l = Logger.getLogger(getCategory(curclass));
        return l.isEnabledFor(lvl);
        
    }
        
    static void loggit(Object categoryobject, String message, Throwable t, String level)
    {
        String category = getCategory(categoryobject);
        Logger l = Logger.getLogger(category);
        Level lev = Level.toLevel(level);
        l.log(lev, message, t);
    }
    
    public static long curtime() { return (new Date()).getTime(); }

}
