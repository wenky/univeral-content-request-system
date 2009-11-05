package org.tarantula.component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionError;
import org.tarantula.component.interfacedefinition.PageComponentInterface;

// parameter validations...
// test: range, greater, lesser, set, regex, required
//       isdate, istype

public class Validate implements PageComponentInterface 
{
    public String getNickname() { return "Validate"; }

    public void init()
    {
        // do nothing for now...
    }
    
    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // the tests parameter must be a ValueList literal in the Input definition (Input.Type = "list")
        List testlist = (List)arguments.get("ValidationTests");
        List errors = new ArrayList();
        for (int i = 0; i < testlist.size(); i++)
        {
            Properties testdetail = (Properties)testlist.get(i);
            String testtype = testdetail.getProperty("type");
            String testfield = testdetail.getProperty("field");
            String fieldvalue = (String)arguments.get(testfield);
            if ("required".equals(testtype))
            {
                if (fieldvalue == null || "".equals(fieldvalue))
                {
                    errors.add(new ActionError("errors.required",testfield));
                }
            }
            else if ("datatype".equals(testtype))
            {
                String datatype = testdetail.getProperty("datatype");
                if ("integer".equals(datatype))
                    try {Integer.parseInt(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.integer",testfield));}
                else if ("long".equals(datatype))
                    try {Long.parseLong(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.long",testfield));}
                else if ("byte".equals(datatype))
                    try {Byte.parseByte(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.byte",testfield));}
                else if ("short".equals(datatype))
                    try {Short.parseShort(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.short",testfield));}
                else if ("double".equals(datatype))
                    try {Double.parseDouble(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.double",testfield));}
                else if ("float".equals(datatype))
                    try {Float.parseFloat(fieldvalue);} catch (NumberFormatException nfe) {errors.add(new ActionError("errors.float",testfield));}                    
            }
            else if ("regex".equals(testtype))
            {
                String regex = testdetail.getProperty("pattern"); 
                if (!Pattern.compile(regex).matcher(fieldvalue).matches())
                {                    
                    errors.add(new ActionError(testdetail.getProperty("message"),testfield));                    
                }                
            }
            else if ("date".equals(testtype))
            {
                String dateformat = testdetail.getProperty("format");
                SimpleDateFormat sdf = null;
                if (dateformat == null)
                    sdf = new SimpleDateFormat("MM/dd/yyyy");
                else
                    sdf = new SimpleDateFormat(dateformat);
                try {
                    if (sdf.parse(fieldvalue) == null)
                        errors.add(new ActionError("errors.date",testfield));                    
                } catch (ParseException pe) {errors.add(new ActionError("errors.date",testfield));}
            }
        }
        return "";
    }
}
