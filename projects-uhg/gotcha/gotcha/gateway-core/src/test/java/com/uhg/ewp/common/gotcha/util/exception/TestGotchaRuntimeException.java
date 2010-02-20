package com.uhg.ewp.common.gotcha.util.exception;


public class TestGotchaRuntimeException
{
    public static void main(String[] args)
    {
        TestGotchaRuntimeException tester = new TestGotchaRuntimeException();
     
        System.out.println("testSimple: "+tester.testSimpleCallstackDetection());
        System.out.println("testStatic: "+testStaticCallstackDetection());
        System.out.println("testInner: "+tester.testInnerCallstackDetection());
        System.out.println("testInnerInner: "+tester.testInnerInnerCallstackDetection());
        
    }
    
    public String testSimpleCallstackDetection()
    {
        try {
            triggerSimpleRuntimeException();
        } catch (GotchaRuntimeException gre) {
            String message = gre.getMessage();
            return message;
        }
        return "impossible";
    }
    
    public void triggerSimpleRuntimeException()
    {
        throw new GotchaRuntimeException("TEST-EX-SIMPLE", "Simple triggered exception");
    }

    
    public static String testStaticCallstackDetection()
    {
        try {
            triggerRuntimeExceptionStatic();
        } catch (GotchaRuntimeException gre) {
            String message = gre.getMessage();
            return message;
        }
        return "impossible";
    }    

    public static void triggerRuntimeExceptionStatic()
    {
        throw new GotchaRuntimeException("TEST-EX-STATIC");
    }
    
    
    public String testInnerCallstackDetection() {
        try {
            class Innertest
            {
                void triggerRuntimeExceptionInner() { throw new GotchaRuntimeException("TEST-EX-INNER", "Inside Job"); }
            }
            Innertest it = new Innertest();
            it.triggerRuntimeExceptionInner();
        } catch (GotchaRuntimeException gre) {
            String message = gre.getMessage();
            return message;
        }
        return "impossible";
    }    

    public String testInnerInnerCallstackDetection() {
        try {
            class Innertest
            {                
                void innerMethod() { triggerRuntimeExceptionInner(); }
                void triggerRuntimeExceptionInner() { throw new GotchaRuntimeException("TEST-EX-2INNER"); }
            }
            Innertest it = new Innertest();
            it.innerMethod();
        } catch (GotchaRuntimeException gre) {
            String message = gre.getMessage();
            return message;
        }
        return "impossible";
    }    

    
}
