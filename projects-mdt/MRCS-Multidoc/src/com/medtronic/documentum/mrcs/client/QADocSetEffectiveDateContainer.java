package com.medtronic.documentum.mrcs.client;


/**
 ******************************************************************************
 * Copyright 2002-2005. EMC Corporation.  All Rights Reserved.
 ******************************************************************************
 *
 * Project        WDK
 * File           NewContainer.java
 * Description    Container for New components
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
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.DialogContainer;
import com.documentum.web.formext.session.DocbaseUtils;

/**
 * Container for New components
 *
 * @author
 * @version     $Revision: 1.1 $
 */
public class QADocSetEffectiveDateContainer extends DialogContainer
{
   //--------------------------------------------------------------------------
   // Initialization

   /**
    * Component initialization
    */
   public void onInit(ArgumentList args)
   {
      super.onInit(args);

      String strComponentArgs[] = args.getValues("componentArgs");
      ArgumentList componentArgs = new ArgumentList();
      for (int i=0; i < strComponentArgs.length; i++)
      {
    	  String strEncodedArgs = strComponentArgs[i];
    	  System.out.println("MRCSSetEffectiveDateContainer::EncodedArgs::" + strEncodedArgs);
    	  componentArgs.add(ArgumentList.decode(strEncodedArgs));
      }
      setContainedComponentArgs(componentArgs);      
      
      // set the title text
      Component component = getContainedComponent();
      m_strTitle = component.getString("MSG_TITLE");

      Label labelTitle = (Label)getControl("title", Label.class);
      labelTitle.setLabel(m_strTitle + ": " + getString("MSG_CREATE"));

   }

   /**
    * Handle the onRender event.
    */
   public void onRender()
   {
      super.onRender();

      Label labelTitle = (Label)getControl("title", Label.class);
      String strTabTitle = getContainedComponent().getString("MSG_TITLE");
      if (strTabTitle.equals(m_strTitle) == true)
      {
         strTabTitle = getString("MSG_CREATE");
      }
      labelTitle.setLabel(m_strTitle + ": " + strTabTitle);

   }
   
   //--------------------------------------------------------------------------
   // Protected Methods

   /**
    * Get details message from the exception object
    * @param   exception        The exception object.
    * @return  error messages
    */
   protected String getDetailsMessage(Exception exception)
   {
      return DocbaseUtils.getValidationExceptionMsg(exception);
   }
   
   //--------------------------------------------------------------------------
   // Private Data

   /**
    * Dialog title from the component
    */
   private String m_strTitle = "";


} // end NewContainer


