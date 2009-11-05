package com.medtronic.ecm.documentum.qad.reassigntask;

import java.util.List;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.multiselector.MultiSelector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.ecm.common.Lg;

/**
 * Component class for Tasks To be Reassigned
 * MultiSelector loads with list of task belongs to selected from user.
 * @author molakr1
 *
 */
public class MdtTasksTobeReassigned extends Component {
	public void onInit(ArgumentList arg) {
        /*-INFO-*/Lg.inf("onInit - top (pre-super)");
		// Note: super.onInit must be called for Component derived classes
		super.onInit(arg);

		// get the IDfSessionManager
		IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();

		try {
			//Get the session
			m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase());

			// Initialize and load the task list
	        /*-dbg-*/Lg.dbg("load reassignable tasks");
			loadTasksFromList(((MdtReassignTaskContainer)(this.getContainer())).getUsersTaskList());

		} catch (DfException dfe) {
			throw new WrapperRuntimeException("Failed to initialise data sets",dfe);
		} finally {
			try {sessionManager.release(m_dfSession);} catch (Exception e) {}
		}
        /*-dbg-*/Lg.dbg("onInit done");
	}
	/**
	 * onRender
	 */
	public void onRender() {
		// TODO Auto-generated method stub
		super.onRender();

	}
	
	/** 
	* called from Container
	* Returns selected tasks 
	*/
	public List getSelectedTaskList(){
		return tasksListBox.getSelectedItems();
	}
	
	/**
	 * Loads MultiSelector with list of tasks belongs to selected from user
	 * @param m_taskList
	 */
	public void loadTasksFromList(List m_taskList) {		
        /*-dbg-*/Lg.dbg("lookup userstasklistbox control");
		tasksListBox = (MultiSelector)getControl("userstasklistbox", com.documentum.web.form.control.multiselector.MultiSelector.class);
		tasksListBox.setSelectedItems(null);
		if(m_taskList != null) {
			tasksListBox.setItemsLabel("Available Tasks");
			tasksListBox.getItemsListBoxControl().setToolTip("Available Tasks");
			tasksListBox.setSelectedItemsLabel("Selected Task");
			tasksListBox.getSelectedItemsListBoxControl().setToolTip("Selected Task");
	        /*-dbg-*/Lg.dbg("putting tasklist arraylist into userstasklistbox control");			
			tasksListBox.setItems(m_taskList);
		}
        /*-dbg-*/Lg.dbg("done");            
	}
	
    private IDfSession m_dfSession = null;
    public static final String USER_TASK_LISTBOX = "userstasklistbox";
    public MultiSelector tasksListBox = null;
}
