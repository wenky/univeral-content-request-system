package com.medtronic.documentum.mrcs.client;
import java.util.ArrayList;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.DataListBox;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.SessionManagerHttpBinding;

public class QADocSetEffectiveDate extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList m_arrSelectedObjects = null;
	
	public void onInit(ArgumentList arg) {
		
		// Note: super.onInit must be called for Component derived classes
		super.onInit(arg);
		
		String [] vals = arg.getValues("objectId");
		m_arrSelectedObjects = new ArrayList();
		for (int i = 0; i < vals.length; i++) {		
			m_arrSelectedObjects.add(vals[i]);
			System.out.println("ID::" + vals[i]);		
		}
		
		// set the connection on the datagrid control
		IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();

		try {
			m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase());
		} catch (DfException dfe) {
			throw new WrapperRuntimeException("Failed to initialise data sets",
					dfe);
		} finally {
			if (m_dfSession != null) {
				sessionManager.release(m_dfSession);
			}
		}
		// Process arguments
	}

	public void onRender() {
//		 retrieve docbase connection
	      IDfSessionManager sessionManager = SessionManagerHttpBinding.getSessionManager();
	      try
	      {
	         m_dfSession = sessionManager.getSession(SessionManagerHttpBinding.getCurrentDocbase()); 
	         String strIDs = "";
	         for (int i = 0; i < m_arrSelectedObjects.size(); i++) {
	        	 String strValue = (String)m_arrSelectedObjects.get(i);
	        	 strIDs = strIDs + "'" + strValue + "',";
	         }
	         strIDs = strIDs.substring(0, strIDs.length() -1);
	         System.out.println("IDs::" + strIDs);
	         
	         // set the connection on the datagrid controls
	         Datagrid dataGrid = (Datagrid)getControl(DATAGRID1, Datagrid.class);
	         String strQuery ="SELECT r_object_id, object_name, r_object_type, owner_name, m_effective_date FROM m_mrcs_qadoc WHERE r_object_id IN ("+ strIDs +")";
	         dataGrid.getDataProvider().setDfSession(getDfSession());
	         dataGrid.getDataProvider().setQuery(strQuery);
	         
	      }
	      catch (DfException dfe)
	      {
	         throw new WrapperRuntimeException("Failed to set connection", dfe);
	      }
	}
	
	
	/**
	 * Called when the changes are to be canceled. E.g. when the user presses
	 * 'Cancel' or 'Close'
	 * 
	 * @return Whether the changes where successfully canceled
	 */
	public boolean onCancelChanges() {		
		return true;
	}

	/**
	 * Called when the changes are to be canceled. E.g. when the session is to
	 * be invalidated.
	 * 
	 * @return Whether the changes where successfully canceled
	 */
	public boolean onCancelChanges(IDfSessionManager manager) {
		return true;
	}

	private IDfSession m_dfSession = null;

	////////////////////////////////////////////////////////////////////////////////
	// Public data
	   
	static final public String DATAGRID1 = "docgrid1";
	
}
