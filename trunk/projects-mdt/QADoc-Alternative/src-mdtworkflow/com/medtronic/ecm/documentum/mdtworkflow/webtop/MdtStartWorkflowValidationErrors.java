package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;

public class MdtStartWorkflowValidationErrors extends Component 
{
    public void onInit(ArgumentList args) 
    {
        super.onInit(args);
        String objid = args.get("objectId");
        String[] errormsgs = args.getValues("validationerrors");
        
        Label errordisplay = (Label)getControl("validationerrors",Label.class);        
        String errorstring = "";
        for (int errs = 0; errs < errormsgs.length; errs++)
        {
            errorstring += errormsgs[errs]+"<BR>";
        }
        errordisplay.setLabel(errorstring);
        
    }
    
    public void onOk(Button button, ArgumentList args) 
    {
        this.setComponentReturn();
    }
   

}
