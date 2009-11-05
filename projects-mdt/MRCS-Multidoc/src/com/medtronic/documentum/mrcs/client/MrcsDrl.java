package com.medtronic.documentum.mrcs.client;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Button;
import com.documentum.web.formext.drl.DRLComponent;

public class MrcsDrl extends DRLComponent 
{
	
    public void onInit(ArgumentList args)
    {
    	super.onInit(args);
    	Button editbutton = (Button)this.getControl("mrcsedit", Button.class);
    	if (editbutton != null)
    	{
    		editbutton.setEnabled(false);
    	}
    }
    
    public void onMrcsEditClicked(Button ourbutton,ArgumentList args)
    {
    	super.onEditClicked(ourbutton,args);
    }

	

}
