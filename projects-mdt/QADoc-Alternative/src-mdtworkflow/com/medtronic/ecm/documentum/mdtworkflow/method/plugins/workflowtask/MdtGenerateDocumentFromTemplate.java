package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.common.VelocityExecute;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.util.DctmUtils;
import com.medtronic.ecm.documentum.util.VeloUtils;

public class MdtGenerateDocumentFromTemplate implements IMdtWorkflowAction
{
    
    public void execute(IDfSessionManager sessionmgr, String docbase, String mdtapp, IDfSysObject formobj, List attachments, IDfWorkitem workitem, Map methodparameters, Map pluginconfig)
    {
        IDfSession session = null;
        try {
            session = sessionmgr.getSession(docbase);
            /*-dbg-*/Lg.wrn("get most recent versions of attachments");
            attachments = AttachmentUtils.getMostRecent(attachments);
            /*-dbg-*/Lg.wrn("get template path");
            String templatepath = (String)pluginconfig.get("Template");
            String templateformat = (String)pluginconfig.get("Format");
            /*-dbg-*/Lg.wrn("loading %s",templatepath);
            String template = DctmUtils.loadFileContents(session,templatepath);
            /*-dbg-*/Lg.wrn("construct velocity context with standard objects");
            VelocityContext vctx = VelocityExecute.createContext(
                    "session",session,
                    "form",formobj,
                    "workitem",workitem,
                    "attachments",attachments,
                    "methodparams",methodparameters,
                    "veloutils",new VeloUtils());
            /*-dbg-*/Lg.wrn("perform substitution");
            String result = VelocityExecute.generateHTML(vctx,template);
            /*-dbg-*/Lg.wrn("set as context");
            DctmUtils.setTextContent(formobj, result, templateformat);
            /*-dbg-*/Lg.wrn("done");
        } catch (DfException dfe) {
            throw EEx.create("GenFormHTML-DFE", "DfException encountered in form generation - probably a bad template object", dfe);
        } finally {
            try {if(session != null)sessionmgr.release(session);}catch(Exception e) {}
        }
        
    }
    
    public static void main(String[] args) throws Exception
    {
        try { 
            MdtGenerateDocumentFromTemplate obj = new MdtGenerateDocumentFromTemplate(); 
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_dev", loginInfoObj);             
            IDfSession sess = sMgr.getSession("mqadoc_dev");
            
            String mdtapp = "qad";
            IDfSysObject formobj = (IDfSysObject)sess.getObject(new DfId("09017f4480061787"));
            Map context = new HashMap();
            context.put("Template","/Admin-MDT/QAD CR Test Template");
            context.put("Format","html");
                        
            VeloUtils vutil = new VeloUtils();
            List atts = vutil.getRepeatingAttributeValues(formobj, "m_attachments");
            for (int i=0; i < atts.size(); i++)
            {
                atts.set(i,sess.getObjectByQualification("dm_document where object_name = '"+atts.get(i)+"'"));
            }
                
            obj.execute(sMgr, "mqadoc_dev", mdtapp, formobj, atts, null, null, context);
            
            sMgr.release(sess);
        } catch (Exception ez) {
            int i = 1;
            i++;
            
        }        
        
    }

}
