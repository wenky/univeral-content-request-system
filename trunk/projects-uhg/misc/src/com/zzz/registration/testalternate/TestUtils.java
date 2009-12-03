package com.zzz.registration.testalternate;

import static org.easymock.EasyMock.expect;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.test.MockParameterMap;

public class TestUtils
{   
    
    public static Map<String,Object> createMap(Object... setters)
    {
        try { 
            Map<String,Object> map = new HashMap<String,Object>();
            populateMap(map,setters);
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Error in populating map",e);
        }
    }

    
    public static void populateMap(Map<String,Object> target, Object... setters) throws Exception
    {
        if (setters != null) {
            for (int i=0; i < setters.length/2; i++) {
                String propname = (String)setters[i*2];
                Object propval = setters[i*2+1];
                target.put(propname,propval);
            }
        }
    }

    
    public static void populateBean(Object target, Object... setters) throws Exception
    {
        if (setters != null) {
            for (int i=0; i < setters.length/2; i++) {
                String propname = (String)setters[i*2];
                Object propval = setters[i*2+1];
                invokeSetter(target,propname,propval);
            }
        }
    }

    public static void populateBean(Object target, Map<String,Object> setters) throws Exception
    {
        if (setters != null) {
            for (String propname : setters.keySet()) {
                Object propval = setters.get(propname);
                invokeSetter(target,propname,propval);
            }
        }
    }

    public static void populateBeanLoose(Object target, Object... setters) throws Exception
    {
        if (setters != null) {
            for (int i=0; i < setters.length/2; i++) {
                String propname = (String)setters[i*2];
                Object propval = setters[i*2+1];
                invokeSetterLoose(target,propname,propval);
            }
        }
    }

    public static void populateBeanLoose(Object target, Map<String,Object> setters) throws Exception
    {
        if (setters != null) {
            for (String propname : setters.keySet()) {
                Object propval = setters.get(propname);
                invokeSetterLoose(target,propname,propval);
            }
        }
    }

    
    public static void prepareGetterExpects(Object target, Object... setters) throws Exception
    {
        if (setters != null) {
            for (int i=0; i < setters.length/2; i++) {
                String propname = (String)setters[i*2];
                Object propval = setters[i*2+1];
                doExpectGet(target,propname,propval);
            }
        }
    }

    public static void prepareGetterExpects(Object target, Map<String,Object> setters) throws Exception
    {
        if (setters != null) {
            for (String propname : setters.keySet()) {
                Object propval = setters.get(propname);
                doExpectGet(target,propname,propval);
            }
        }
    }

    public static void prepareGetterExpectsLoose(Object target, Object... setters) throws Exception
    {
        if (setters != null) {
            for (int i=0; i < setters.length/2; i++) {
                String propname = (String)setters[i*2];
                Object propval = setters[i*2+1];
                doExpectGetLoose(target,propname,propval);
            }
        }
    }

    public static void prepareGetterExpectsLoose(Object target, Map<String,Object> setters) throws Exception
    {
        if (setters != null) {
            for (String propname : setters.keySet()) {
                Object propval = setters.get(propname);
                doExpectGetLoose(target,propname,propval);
            }
        }
    }

    public static void doExpectGet(Object target, String property, Object value) throws Exception
    {
        String methodname = "get"+property.substring(0,1).toUpperCase()+property.substring(1);
        Method[] methods = target.getClass().getMethods();
        boolean found = false;
        for (int i=0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodname)) {
                Method match = methods[i];
                // see if return type is compatible with current expectation value
                Class rettype = match.getReturnType();
                value = convertObject(rettype,value);
                expect(methods[i].invoke(target)).andReturn(value);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Property/Accessor "+methodname+" not found on target object");
        }
    }

    public static void doExpectGetLoose(Object target, String property, Object value) throws Exception
    {
        String methodname = "get"+property.substring(0,1).toUpperCase()+property.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i=0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodname)) {
                Method match = methods[i];
                // see if return type is compatible with current expectation value
                Class rettype = match.getReturnType();
                value = convertObject(rettype,value);
                expect(methods[i].invoke(target)).andReturn(value);
                break;
            }
        }
    }

    
    public static void invokeSetter(Object target, String property, Object value) throws Exception
    {
        String methodname = "set"+property.substring(0,1).toUpperCase()+property.substring(1);
        Method[] methods = target.getClass().getMethods();
        boolean found = false;
        for (int i=0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodname)) {
                Method match = methods[i];
                // see if return type is compatible with current expectation value
                Class settype = (match.getParameterTypes())[0];
                value = convertObject(settype,value);
                methods[i].invoke(target, value);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Property/Accessor "+methodname+" not found on target object");
        }
    }

    // doesn't care if it's not found
    public static void invokeSetterLoose(Object target, String property, Object value) throws Exception
    {
        String methodname = "set"+property.substring(0,1).toUpperCase()+property.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (int i=0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodname)) {
                Method match = methods[i];
                // see if return type is compatible with current expectation value
                Class settype = (match.getParameterTypes())[0];
                value = convertObject(settype,value);
                methods[i].invoke(target, value);
                break;
            }
        }
    }

    public static Object convertObject(Class settype, Object value)
    {
        
        if (!settype.isInstance(value)) {
            if (value instanceof String) {
                // attempt translation of value to appropriate type
                if (settype.equals(Integer.class)) 
                    value = Integer.parseInt((String)value);
                if (settype.equals(Float.class)) 
                    value = Float.parseFloat((String)value);
                if (settype.equals(Double.class))
                    value = Double.parseDouble((String) value);
                if (settype.equals(Long.class))
                    value = Long.parseLong((String) value);
                if (settype.equals(Boolean.class))
                    value = Boolean.parseBoolean((String) value);
            }
//            if (value instanceof Date) {
//                if (settype.equals(MonthDayYearDateHolder.class)) {
//                    MonthDayYearDateHolder mdy = new MonthDayYearDateHolder();
//                    mdy.setDate((Date)value);
//                    value = mdy;
//                }
//            }
//            if (value instanceof MonthDayYearDateHolder) {
//                if (settype.equals(Date.class)) {
//                    MonthDayYearDateHolder mdy = (MonthDayYearDateHolder)value;
//                    value = mdy.getDate();
//                }
//            }
        }
        return value;
    }

    public static MockParameterMap convertToParameterMap(Map<String,Object> map)
    {
        MockParameterMap pmap = new MockParameterMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof String) {
                pmap.put(key, (String)value);
//            } else if (value instanceof MonthDayYearDateHolder) {
//                MonthDayYearDateHolder mdy = (MonthDayYearDateHolder)value;
//                pmap.put(key+".month", mdy.getMonth().toString());
//                pmap.put(key+".day", mdy.getDay().toString());
//                pmap.put(key+".year", mdy.getYear());
            } else if (value instanceof Date) {
                Date mdy = (Date)value;
                pmap.put(key+".month", ""+mdy.getMonth());
                pmap.put(key+".day", ""+mdy.getDay());
                pmap.put(key+".year", ""+mdy.getYear());
            } else {pmap.put(key, value.toString()); }
        }
        return pmap;        
    }

    public static Map<String,Object> copyExcluding(Map<String,Object> map,String... excluding)
    {
        Map<String,Object> newmap = new HashMap<String,Object>();
        newmap.putAll(map);
        if (excluding != null) {
            for (String key : excluding) {
                newmap.remove(key);
            }
        }
        return newmap;
    }

    public static Map<String,Object> copyClearing(Map<String,Object> map,String... excluding)
    {
        Map<String,Object> newmap = new HashMap<String,Object>();
        newmap.putAll(map);
        if (excluding != null) {
            for (String key : excluding) {
                newmap.put(key,null);
            }
        }
        return newmap;
    }

}
