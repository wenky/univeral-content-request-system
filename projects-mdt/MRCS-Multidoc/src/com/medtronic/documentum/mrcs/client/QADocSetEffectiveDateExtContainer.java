package com.medtronic.documentum.mrcs.client;

//public class  {

/**
 ******************************************************************************
 * Copyright 2002-2005. EMC Corporation.  All Rights Reserved.
 ******************************************************************************
 *
 * Project        WDK
 * File           NewFolderContainer.java
 * Description    Container for New Folder
 * Created on     April 22, 2002
 * Tab width      3
 *
 ******************************************************************************
 *
 * PVCS Maintained Data
 *
 * Revision       $Revision: 1.1 $
 * Modified on    $Date: 2007/06/20 22:17:54 $
 *
 * Log at EOF
 *
 ******************************************************************************
 */

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.session.IHttpSessionManagerUnboundListener;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.fc.client.IDfSessionManager;

/**
 * Container for New Folder
 *
 * @author
 * @version     $Revision: 1.1 $
 */
public class QADocSetEffectiveDateExtContainer extends QADocSetEffectiveDateContainer implements IHttpSessionManagerUnboundListener
{
   //--------------------------------------------------------------------------
   // Initialization

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
    * Component initialization
    */
   public void onInit(ArgumentList args)
   {
      super.onInit(args);
   }

   //--------------------------------------------------------------------------
   // Public methods

   /**
    * onOk Event handler - called when user presses 'Ok'
    *
    * @param button     the button
    * @param args       the arguments
    */
   public void onOk(Control button, ArgumentList args)
   {
	   if ( canCommitChanges() && onCommitChanges())
	   {
		   // apply effective dates...
		   
	       // remove this instance as an http session unbound listener as we have completed the request.
	       SessionManagerHttpBinding.removeHttpSessionUnboundListener(this);            
	       setComponentReturn();
      }
   }

   /**
    * Called when the changes are to be canceled. E.g. when the user presses 'Cancel'
    * or 'Close'
    *
    * @return Whether the changes where successfully canceled
    */
   public boolean onCancelChanges()
   {
      boolean fCan = super.onCancelChanges();
      if (fCan)
      {
         // remove this instance as an http session unbound listener as we have completed the request.
         SessionManagerHttpBinding.removeHttpSessionUnboundListener(this);
      }

      return fCan;
   }

   /**
    * Session 'unBound' notification before an IDfSessionManager is invalidated.
    *
    * @param manager the session manager
    */
   public void unbound(IDfSessionManager manager)
   {
      if (m_mrcsSetEffectiveDateComponent != null)
      {
    	  m_mrcsSetEffectiveDateComponent.onCancelChanges(manager);
      }
   }

   //--------------------------------------------------------------------------
   // Private Data

   // the component for the object creation
   QADocSetEffectiveDate m_mrcsSetEffectiveDateComponent = null;   
   
} // end NewFolderContainer


