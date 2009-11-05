package com.medtronic.ecm.documentum.qad.reassigntask;

import java.util.Iterator;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;

/**
 * Component class for Task Confirmation
 * sets fromuser,taskname(objectnames),to user
 * @author molakr1
 *
 */
public class MdtReassignTaskConfirmation extends Component{
	public void onInit(ArgumentList arg) {

		// Note: super.onInit must be called for Component derived classes
		super.onInit(arg);

		// set the connection on the datagrid control
		IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();

		// Load the lIST  with list of USERS	
		try {
			//Get the session
			m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase());

			// Initialize and load the values
			
			//Get the Task From User
			String strTaskFromUser 	= ((MdtReassignTaskContainer)(this.getContainer())).getTaskFromUser();
			//Get the Task name(Concatinated object name)
			String strTaskName 		= ""; 
            Iterator iterSelectedTask = ((MdtReassignTaskContainer)(this.getContainer())).lstSelectedTask.iterator();
            while(iterSelectedTask.hasNext()) {
                Object[] strWorkItemId = (Object[])iterSelectedTask.next();
                strTaskName = (String)strWorkItemId[1];
            }
			    
			//Get the Task To User
			String strTaskToUser 	= ((MdtReassignTaskContainer)(this.getContainer())).getTasktoUserName();

			//Set Task From User
			Text fromUserName = (Text)getControl("MSG_FROM_USER_NAME", com.documentum.web.form.control.Text.class);			
			fromUserName.setValue(strTaskFromUser);
			fromUserName.setEnabled(false);
			//Set Task name
			Text taskName = (Text)getControl("MSG_ACTUAL_TASK_NAME", com.documentum.web.form.control.Text.class);
			taskName.setValue(strTaskName);
			taskName.setEnabled(false);

			//Set Task To User
			Text toUserName = (Text)getControl("MSG_TO_USER_NAME", com.documentum.web.form.control.Text.class);
			toUserName.setValue(strTaskToUser);
			toUserName.setEnabled(false);
			
		} catch (DfException dfe) {
			throw new WrapperRuntimeException("Failed to initialise data sets",dfe);
		} finally {
			if (m_dfSession != null) {
				sessionManager.release(m_dfSession);
			}
		}
	}

	public void onRender() {
		// TODO Auto-generated method stub
		super.onRender();
	}
    private IDfSession m_dfSession = null;
    private ListBox taskListBox = null;

}
