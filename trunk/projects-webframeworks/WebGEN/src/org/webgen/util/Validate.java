package org.webgen.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;


public class Validate 
{
	
	// various validation helper functions
	//	 test: range, greater, lesser, set, regex, required
	//	       isdate, istype
	
	// fine grained tests - datatype tests
	public static boolean isByte(String fieldvalue) {try {Byte.parseByte(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isShort(String fieldvalue) {try {Short.parseShort(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isInt(String fieldvalue) {try {Integer.parseInt(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isLong(String fieldvalue) {try {Long.parseLong(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isBigInt(String fieldvalue) {try {new BigInteger(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isFloat(String fieldvalue) {try {Float.parseFloat(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isDouble(String fieldvalue) {try {Double.parseDouble(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }
	public static boolean isBigDecimal(String fieldvalue) {try {new BigDecimal(fieldvalue);} catch (NumberFormatException nfe) {return false;} return true; }

	public static boolean isDate(String fieldvalue) {return isDate(fieldvalue,null); }
	public static boolean isDate(String fieldvalue, String dateformat)
	{
	    SimpleDateFormat sdf = null;
	    if (dateformat == null)
	        sdf = new SimpleDateFormat("MM/dd/yyyy");
	    else
	        sdf = new SimpleDateFormat(dateformat);
	    try {
	        if (sdf.parse(fieldvalue) == null)
	        	return false;
	    } catch (ParseException pe) {return false;}
	    return true;
	}

	// ahhhh, isEmpty()
	// - strict trims
	// - nonstrict doesn't trim
	public static boolean isEmpty(String fieldvalue) { return (fieldvalue == null || "".equals(fieldvalue)); }
	public static boolean isEmptyTrimmed(String fieldvalue) { return (fieldvalue == null || "".equals(fieldvalue.trim())); }
	
	// match regex
	public static boolean matchRegex(String fieldvalue, String regex) {	return (Pattern.compile(regex).matcher(fieldvalue).matches()); }
	
}
