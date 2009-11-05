/*
 * Created on Feb 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: DefaultPromotionRule.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.tbo;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DefaultPromotionRule implements PromotionRule {

	/**
	 * 
	 */
	public DefaultPromotionRule() {
		super();
	}
	

	/* (non-Javadoc)
	 * @see com.medtronic.documentum.mrcs.tbo.PromotionRule#getEffectiveDate(com.documentum.fc.common.DfTime)
	 */
	public DfTime getEffectiveDate(boolean docEff, DfTime effDate) {
	    DfTime currDate = new DfTime();
	    IDfTime dtime = null;
	    
	    System.out.println(">>>>>>DefaultPromotionRule DOC Properties getEffectiveDate>>>>>> effDate  " + effDate);
		IDfClientX clientx = new DfClientX();
	    
        if (docEff) {
            // calculate 
    		dtime = clientx.getTime("02/16/2005 16:48:10", DfTime.DF_TIME_PATTERN44);//"mm/dd/yyyy hh:mi:ss"
    		
        }//or allow promotes with Tolerance
        else {
            if((effDate.isNullDate()) || (effDate.compareTo(currDate) == -1)){
        
            Calendar calendar = new GregorianCalendar();    
            Date dt = new Date ();
            calendar.setTime(dt);
            calendar.set(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)+1,
                    calendar.get(Calendar.SECOND));
            	
            	//dt.setMinutes(dt.getMinutes()+5);
                //dt.setSeconds(dt.getSeconds()+20);        		
            	//dtime = new DfTime(dt);

            //format to match DTime "MM/dd/yyyy HH:mm:ss"
            	String pattern = "MM/dd/yyyy HH:mm:ss";
            	SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            	String dtCntrct = formatter.format(calendar.getTime());
            	System.out.println(pattern + " " + dtCntrct);

            	dtime = new DfTime(dtCntrct,DfTime.DF_TIME_PATTERN44);

        		System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME dtime y " + dtime.getYear());
        	    System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME dtime m " + dtime.getMonth());
        	    System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME dtime d " + dtime.getDay());
        	    System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME  dtime hr " + dtime.getHour());
        	    System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME dtime min " + dtime.getMinutes());
        	    System.out.println(">>>>>>DefaultPromotionRule  EFFECTIVE TIME dtime sec " + dtime.getSeconds());

            }
        }
	    System.out.println(">>>>>>DefaultPromotionRule FINAL EFFECTIVE TIME   " + (DfTime)dtime);
        return (DfTime)dtime; 
	}

}
