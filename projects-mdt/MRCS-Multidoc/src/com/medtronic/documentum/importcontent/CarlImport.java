/*
 * Created on Nov 24, 2004
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

 Filename       $RCSfile: CarlImport.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:41 $

***********************************************************************
*/

package com.medtronic.documentum.importcontent;


/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CarlImport extends com.documentum.webcomponent.library.importcontent.ImportContent
       implements Import
{
    String[] m_acceptedFormats;
    String   m_subject;
    String[] m_keywords;
    
    
    
    public void setSubject(String subject)
    {
        int c=1;
        c=c++;
    }
    public void setKeywords (String[] keywords)
    {
        int a=1;
        a = a+1;
    }
    public void setAcceptedFormats (String[] formats)
    {
        int b=2;
        b = b+2;
    }
    public String getAcceptedFormats()
    {
        return "";
    }
    public boolean isValidFormat (String format)
    {
        return true;
    }
    
}
