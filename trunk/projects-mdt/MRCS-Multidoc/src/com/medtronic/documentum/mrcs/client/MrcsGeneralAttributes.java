/*
 * Created on Jun 1, 2005
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

 Filename       $RCSfile: MrcsGeneralAttributes.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/15 17:31:06 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Checkbox;
import com.documentum.webcomponent.library.attributes.Attributes;

/**
 * @author muellc4
 *
 * this overrides Attributes in order to disable ShowAll logic...
 *
 */
public class MrcsGeneralAttributes extends Attributes
{

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        
        // disable showall regardless...
        // admins can use a different webtop client if they 
        // really need to showall/modify...
        Checkbox checkbox = (Checkbox)getControl("show_all", com.documentum.web.form.control.Checkbox.class);
        if (checkbox != null) checkbox.setVisible(false);
    }
    
}
