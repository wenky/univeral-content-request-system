package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.CollaborationService;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.docbase.FolderUtil;

public class QADocApprovalComponent extends Component
{
	String m_strObjectId = null;
	public static String GRID_NAME = "hubbahubbabubba";
	public static String SIGNATURES_GRID = "heyheybooboo";
	
	

    public void onInit(ArgumentList args)
    {
        m_strObjectId = args.get("objectId");
        if(m_strObjectId == null)
            throw new IllegalArgumentException(getString("MSG_MISSING_MANDATORY_PARAMS"));
        boolean fIsFolderType = FolderUtil.isFolderType(m_strObjectId);
        boolean bIsNotePageType = CollaborationService.isNotePageType(m_strObjectId);
        
        // get query and references to datagrid
        Datagrid approvalgrid = (Datagrid)getControl(GRID_NAME,Datagrid.class);
        
        IDfDocument doc = null;
        String qrystr = null;
        try { 
        	doc = (IDfDocument)getDfSession().getObject(new DfId(m_strObjectId));
        
	        qrystr = "SELECT r_object_id, object_name, r_version_label FROM m_mrcs_document(ALL) where r_object_id in " +
	           		    	"(SELECT child_id FROM dm_relation where child_id not in (select r_object_id from m_mrcs_document(all) where i_chronicle_id = '"+doc.getChronicleId()+"') and parent_id in " +
	        		        "(SELECT parent_id FROM dm_relation where child_id in (select r_object_id from m_mrcs_document(all) where i_chronicle_id = '"+doc.getChronicleId()+"') and relation_name = 'qadoc_form_relation'))";
        } catch (Exception e) { throw new IllegalArgumentException("bad object id"); }
        approvalgrid.getDataProvider().setQuery(qrystr);
        approvalgrid.getDataProvider().setDfSession(getDfSession());
        
        // signing reasons
        try { 
	        qrystr = 
	        	"SELECT user_name, string_1, string_2 FROM dm_audittrail WHERE event_name = 'dm_addesignature' and audited_obj_id in "+
	        		"(SELECT max(audited_obj_id) from dm_audittrail where event_name = 'dm_addesignature' and audited_obj_id in (select r_object_id from dm_document(all) where i_chronicle_id = '"+doc.getChronicleId()+"'))";
        } catch (Exception e) { throw new IllegalArgumentException("bad object id"); }
        Datagrid reasonsgrid = (Datagrid)getControl(SIGNATURES_GRID,Datagrid.class);
        reasonsgrid.getDataProvider().setQuery(qrystr);
        reasonsgrid.getDataProvider().setDfSession(getDfSession());
        
        // trigger datagrid refresh?
        super.onInit(args);
    }

    // we shouldn't need anything else, may need to respond to close event...

}
