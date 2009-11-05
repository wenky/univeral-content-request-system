package com.medtronic.ecm.documentum.qad.reassigntask;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.multiselector.MultiSelector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.web.util.DfcUtils;
import com.medtronic.ecm.common.Lg;

/**
 * Component class for Reassign Task To user
 * MultiSelector will be loaded with list of users based on users application.
 * @author molakr1
 *
 */
public class MdtReassignTaskTo extends Component {
	public void onInit(ArgumentList arg) {
        /*-INFO-*/Lg.inf("top, pre-super");            
		// Note: super.onInit must be called for Component derived classes
		super.onInit(arg);

		// set the connection on the datagrid control
		IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();

		// Load the lIST  with list of USERS	
		try {
			//Get the session
			m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase());
			
			// Initialize and load the users list
			MdtReassignReadConfigValues readConfig = new MdtReassignReadConfigValues();
			loadUsersToList(readConfig.getUserNamesFromConfig(m_dfSession));

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
	
	/**
	 * Called from Container List of Selected users
	 * @return List of Selected users
	 */
	public List getSelectedUserToList(){
		return toUserListBox.getSelectedItems();
	}
	/**
	 * MultiSelector loads with list of users
	 * @param m_UserList
	 */
	private void loadUsersToList(List m_UserList) {		

		toUserListBox = (MultiSelector)getControl("touserslistbox", com.documentum.web.form.control.multiselector.MultiSelector.class);
		if(m_UserList != null) {
			toUserListBox.setItemsLabel("Available Users");
			toUserListBox.getItemsListBoxControl().setToolTip("Available Users");
			toUserListBox.setSelectedItemsLabel("Selected user");
			toUserListBox.getSelectedItemsListBoxControl().setToolTip("Selected user");
			toUserListBox.setItems(m_UserList);
		}
	}

	private IDfSession m_dfSession = null;
	public static final String TO_USERS_LISTBOX = "touserslistbox";
	public MultiSelector toUserListBox = null;
}
