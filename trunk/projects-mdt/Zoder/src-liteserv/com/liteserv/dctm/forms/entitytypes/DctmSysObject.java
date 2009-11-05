package com.liteserv.dctm.forms.entitytypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfValidator;
import com.documentum.fc.client.IDfValueAssistance;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfProperties;
import com.liteserv.forms.FormDefinition;
import com.liteserv.forms.FormField;
import com.liteserv.forms.IEntityType;

public class DctmSysObject implements IEntityType
{
    public static String[] datatypes = {"bool","int","string","string","datetime","double"};
    public String typename = "";
    public String application = "webtop";
    public FormDefinition getFormDefinition(String type, Map context)
    {
        FormDefinition formdef = new FormDefinition();
        // get session so we can query the data dictionary
        IDfSessionManager smgr = (IDfSessionManager)context.get("DctmSessionManager");
        String docbase = (String)context.get("DctmDocbase");
        IDfSession session = null;
        try { 
            session = smgr.getSession(docbase);
            
            // for these entities, type == DCTM object type
            // TODO: add alternate form definitions 
            String dctmobjecttype = type;
            
            String currentstate = (String)context.get("DctmObjectState");
            if (currentstate == null) {
                currentstate = " "; // that's how DCTM seems to be storing it if there's no state...
            }
            
            String nlskey = (String)context.get("DctmNlsKey");
            if (nlskey == null) {                
                nlskey = "en";
            }
            
            IDfType typeinfo = session.getType(dctmobjecttype);
            IDfValidator validator = typeinfo.getTypeValidator(null, currentstate);
            
            // okay, do we cache this? probably not due to the lifecycle state dependency, unless we cache that too...
            
            String ddquery = "SELECT r_object_id, label_text, help_text, attr_name, not_null, not_null_enf, read_only, is_hidden, is_required, format_pattern, domain_type, cond_value_assist, default_value " +
            		         "FROM dmi_dd_attr_info WHERE type_name = '"+dctmobjecttype+"' AND state_name = '"+currentstate+"' AND nls_key = '"+nlskey+"'";
            
            // get object we are dealing with so we can determine state and check for state-based rules
            // get attribute list for type and state
            // -- SELECT r_object_id, attr_name FROM dmi_dd_attr_info 
            // -- WHERE type_name = 'type' and state_name = 'state' and nls_key = 'en'
            
            // get display order...how?
            // application scope = webtop? where is this stored/controlled?
            
            formdef.Fields = new ArrayList();
            
            IDfQuery qry = new DfQuery();
            qry.setDQL(ddquery);
            IDfCollection resultset = null;
            try {
                resultset = (IDfCollection)qry.execute(session, IDfQuery.DF_READ_QUERY);
                while (resultset.next()) 
                {
                    FormField curfield = new FormField();
                    curfield.Name = resultset.getString("attr_name");                    
                    curfield.isKey = "r_object_id".equals(curfield.Name);
                    curfield.Label = resultset.getString("label_text");
                    curfield.Help  = resultset.getString("help_text");
                    curfield.isEnabled = resultset.getBoolean("read_only");
                    curfield.isVisible = resultset.getBoolean("is_hidden");
                    curfield.isRequired = resultset.getBoolean("is_required");
                    curfield.DefaultValue = resultset.getString("default_value");
                    curfield.Datatype = datatypes[resultset.getInt("domain_type")]; // 0=bool 1=int 2=string 3=id 4=time 5=double
                    curfield.hasValueAssistance = false;
                    if (validator.hasValueAssistance(curfield.Name)) {
                        // properties conditional value assistance depends on...ignore for now...but this is food for thought if the server is invoked to get value assistance
                        curfield.hasValueAssistance = true;
                        IDfProperties dependencies = validator.getValueAssistanceDependencies(curfield.Name);
                        //IDfProperties dependencies = null;                        
                        IDfValueAssistance valueassist = validator.getValueAssistance(curfield.Name, dependencies);
                        IDfList codevalues = valueassist.getActualValues();
                        IDfList displayvalues = valueassist.getDisplayValues();                        
                        curfield.CodeValues = new ArrayList();   // DocbaseAttributeValue.getObjectValueAssistance(), IDfValueAssistance, IDfValidator IDfPersistentObject.getValidator()
                        curfield.DisplayValues = new ArrayList();
                        for (int i=0; i < codevalues.getCount(); i++) {
                            // assume always string for now...
                            String code = codevalues.get(i).toString();
                            String value = displayvalues.get(i).toString();
                            curfield.CodeValues.add(code);
                            curfield.DisplayValues.add(value);
                        }
                        // also: curfield.isValidForObject, curfield.isValidForDependentValues <-- to recheck if the dependent attrs change in value...
                        if (valueassist.isListComplete()) {
                            if (typeinfo.isAttrRepeating(curfield.Name)) {
                                curfield.Widget = "multicombo";
                            } else {
                                curfield.Widget = "combo";
                            }
                        } else {
                            if (typeinfo.isAttrRepeating(curfield.Name)) {
                                // could be checkbox set too...
                                curfield.Widget = "editmulticombo";
                            } else {
                                // could be radioset too...
                                curfield.Widget = "editcombo";
                            }
                            
                        }
                    }
                    curfield.InputMask = resultset.getString("format_pattern");
                    // text, checkbox, date, datetime, combo, editcombo, multicombo, editmulticombo, radio, richtext, clob, blob, image, file, 
                    if (curfield.Widget == null) {
                        if ("bool".equals(curfield.Datatype))
                            curfield.Widget = "checkbox";
                        else if ("datetime".equals(curfield.Datatype))
                            curfield.Widget = "datetime";
                        else
                            curfield.Widget = "text";
                    }
                    
                    // TODO: field-level clientvalidations, servervalidations
                    
                    formdef.Fields.add(curfield);
                }                
            } finally {
                try { resultset.close(); } catch (Exception e) {}
            }
            
            // TODO: template ?scoping rules?, form-level clientvalidations + servervalidations
            
            // com.documentum.web.formext.control.docbase.DocbaseAttribute*
            // DocbaseAttributeValue !!! <-- value assistance
            // iterate on attributes
        } catch (Exception e) {
            // ???
            int i=0;
            i++;
        }finally {
            try {smgr.release(session); } catch (Exception e) {}
        }

            
       return formdef;     
            
    }

    
    
    public static void main(String[] args)
    {
        try { 
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_dev", loginInfoObj);             

            DctmSysObject d = new DctmSysObject();
            
            
            HashMap h = new HashMap();
            h.put("DctmSessionManager", sMgr);
            h.put("DctmDocbase", "mqadoc_dev");
            
            FormDefinition formdef = d.getFormDefinition("mdt_document", h);
            int l=0; 
            l++;
            
        } catch (Exception ez) {
            int i = 1;
            i++;
            
        }        
        
    }
}
