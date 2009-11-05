package com.nighteclipse.zoder.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ZuluDateFormatter 
{
	// 1994-09-27T12:01:01Z
	public static String formatDate(Date date)
	{
	    if (date == null) return "";
	    
		String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		long UTC = date.getTime();
		GregorianCalendar gcal = new GregorianCalendar();
		gcal.setTimeZone(TimeZone.getTimeZone("GMT"));
		gcal.setTimeInMillis(UTC);
		
		DecimalFormat four = new DecimalFormat("0000");
		DecimalFormat two = new DecimalFormat("00");
		
		String formatted = four.format(gcal.get(GregorianCalendar.YEAR))+'-'+
		                   two.format(gcal.get(GregorianCalendar.MONTH))+'-'+
		                   two.format(gcal.get(GregorianCalendar.DAY_OF_MONTH))+'T'+
		                   two.format(gcal.get(GregorianCalendar.HOUR))+':'+
		                   two.format(gcal.get(GregorianCalendar.MINUTE))+':'+
		                   two.format(gcal.get(GregorianCalendar.SECOND))+'Z';
		                   
		
		return formatted;
	}

	public static void main(String[] args)
	{
		String s = null;
		
		Date d = new Date();
		s = formatDate(d);
		
		System.out.println(s);
	}

}
