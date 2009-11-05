package com.medtronic.documentum.mrcs.client;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.DropDownList;
import com.documentum.webtop.webcomponent.inbox.InboxStreamline;

public class MrcsInboxStreamline extends InboxStreamline 
{
	public MrcsInboxStreamline()
	{
		super();
	    setCurrentFilter(DISPLAY_ALL + DISPLAY_ATTACHMENTS);
	    setShowAttachmentInfo(true);
	}
	
	public void onInit(ArgumentList args)
	{
		super.onInit(args);
		DropDownList filters = (DropDownList)this.getControl(FILTER_CONTROL_NAME, DropDownList.class);
		filters.setValue(Integer.toString(DISPLAY_ALL + DISPLAY_ATTACHMENTS));
		onSelectTypeFilter(filters,null);
	}
	
}
