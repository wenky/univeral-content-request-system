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

/**
 * Component for Reassign Task From
 * which will have list of from users list, 
 * and users list is based application user belongs to 
 * @author molakr1
 *
 */
public class MdtReassignTaskFrom extends Component{

	public void onInit(ArgumentList arg) {

		// Note: super.onInit must be called for Component derived classes
		super.onInit(arg);

		// get the sessionManager 
		IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();

		try {
			//Get the session
			m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase());

			// Initialize and load the users list
			MdtReassignReadConfigValues readConfig = new MdtReassignReadConfigValues();
			loadUsersFromList(readConfig.getUserNamesFromConfig(m_dfSession));

		} catch (DfException dfe) {
			throw new WrapperRuntimeException("Failed to initialise data sets",dfe);
		} finally {
			if (m_dfSession != null) {
				sessionManager.release(m_dfSession);
			}
		}
	}
	/**
	 * called on rendering the Component
	 *@param
	 */
	public void onRender() {
		super.onRender();
	}

    public boolean onCommitChanges()    {
        return true;
    }
    
	/**
	 * MultiSelector loads with list of users  
	 * @param m_UserList
	 */
	private void loadUsersFromList(List m_UserList) {		

		MultiSelector fromUserListBox = (MultiSelector)getControl("fromuserslistbox", MultiSelector.class);
		if(m_UserList != null) {
			fromUserListBox.setItemsLabel("Available Users");
			fromUserListBox.getItemsListBoxControl().setToolTip("Available Users");
			fromUserListBox.setSelectedItemsLabel("Selected user");
			fromUserListBox.getSelectedItemsListBoxControl().setToolTip("Selected user");
			fromUserListBox.setItems(m_UserList);
		}
	}

	/**
	 * Called From container 
	 * @param
	 * @return List
	 */
	public List getSelectedUserFromList() {
		MultiSelector fromUserListBox = (MultiSelector)getControl("fromuserslistbox", MultiSelector.class);
		return fromUserListBox.getSelectedItems();
	}
	
    private IDfSession m_dfSession = null;
    public static final String FROM_USERS_LISTBOX = "fromuserslistbox";
    //public MultiSelector fromUserListBox = null;
}
