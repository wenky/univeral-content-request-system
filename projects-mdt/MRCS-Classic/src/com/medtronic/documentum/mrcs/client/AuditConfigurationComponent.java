/*
 * Created on Apr 26, 2005
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

 Filename       $RCSfile: AuditConfigurationComponent.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:42 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import javax.servlet.http.HttpSession;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.component.Component;
import com.medtronic.documentum.mrcs.config.AuditConfiguration;
import com.medtronic.documentum.mrcs.config.MrcsConfigBroker;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AuditConfigurationComponent
    extends Component 
{
    public void onInit(ArgumentList argumentlist)
    {
        String report = AuditConfiguration.auditconfig(getDfSession(),this);
        // just use the simple session
        HttpSession httpsession = this.getPageContext().getSession();
        httpsession.setAttribute("mrcs.auditreport",report);        
    }
    
    public void reloadConfiguration(Control button, ArgumentList argumentlist)
    {
        try {
            MrcsConfigBroker.getConfigBroker().loadConfig();
        } catch (Exception e) {
            
        }
        String report = AuditConfiguration.auditconfig(getDfSession(),this);
        // just use the simple session
        HttpSession httpsession = this.getPageContext().getSession();
        httpsession.setAttribute("mrcs.auditreport",report);        
    }
    
    public void cancel(Control button, ArgumentList argumentlist)
    {
        setComponentReturn();
    }    
}
