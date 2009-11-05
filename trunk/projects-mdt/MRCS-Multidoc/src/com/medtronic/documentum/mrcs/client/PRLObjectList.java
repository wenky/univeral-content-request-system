package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.BookmarkLink;
import com.documentum.web.form.control.Breadcrumb;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.drl.DRLComponent;
import com.documentum.webcomponent.navigation.doclist.DocList;
import com.documentum.webtop.app.AppSessionContext;

public class PRLObjectList extends DocList
{

	public boolean booLoadFail;

    public void onInit(ArgumentList argumentlist)
    {
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - top", null, null);
     	if(argumentlist.get("folderId") == null && argumentlist.get("folderIds") == null && argumentlist.get("folderPath") == null)
        {
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - std args not found, will open root", null, null);
            AppSessionContext appsessioncontext = AppSessionContext.get(getPageContext().getSession());
            if(appsessioncontext.getAppLocation().isDocbaseLocation())
            {
                String s1 = appsessioncontext.getAppLocation().getFolderPath();
                if(s1 != null && s1.length() != 0)
                    argumentlist.replace("folderPath", s1);
            }
        } else
        {
        	String singlefolderid = argumentlist.get("folderId");
        	
        	if (singlefolderid!= null && singlefolderid.length() > 0)
        	{
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - single object id provided, look up first and add the FolderPath argument", null, null);
                String s2 = FolderUtil.getFullFolderPathFromIds(singlefolderid);
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - folderpath: "+s2, null, null);
                argumentlist.replace("folderPath", s2);
        		
        	}
            String s = argumentlist.get("folderIds");
            if(s != null && s.length() != 0)
            {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - object ids provided, look up first and add the FolderPath argument", null, null);
                String s2 = FolderUtil.getFullFolderPathFromIds(s);
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - folderpath: "+s2, null, null);
                argumentlist.replace("folderPath", s2);
            }

            if(FolderUtil.getFolderId(argumentlist.get("folderPath")) == null)
            {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onInit - unable to look up the folderpath argument - not present", null, null);
				booLoadFail = true;
			}

        }
        super.onInit(argumentlist);
    }


    public void onRender()
    {
        super.onRender();
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLObjectList.onRendoer - folderpath load fail? "+booLoadFail, null, null);
		if(booLoadFail == true)
		{
			setComponentPage("loadfail");
			booLoadFail = false;
		}
    }


    public void onClickFolderPath(Breadcrumb breadcrumb, ArgumentList argumentlist)
    {
        super.onClickFolderPath(breadcrumb, argumentlist);
        ArgumentList argumentlist1 = new ArgumentList();
        if(getFolderPath() != null)
            argumentlist1.add("ids", FolderUtil.getFolderIdsFromPath(getFolderPath()));
        setClientEvent("onClickAbsolutePath", argumentlist1);
    }

    public void onRefreshData()
    {
        super.onRefreshData();
        ArgumentList argumentlist = new ArgumentList();
        argumentlist.add("docbase", getCurrentDocbase());
        setClientEvent("treeInvalidated", argumentlist);
    }

    protected void updateControlsFromPath(String s)
    {
        super.updateControlsFromPath(s);
        String s1 = "";
        if(super.m_strFolderId != null && super.m_strFolderId.length() > 0)
            s1 = DRLComponent.constructDRL(super.m_strFolderId, null, null, this);
        BookmarkLink bookmarklink = (BookmarkLink)getControl("bookmark", com.documentum.web.form.control.BookmarkLink.class);
        bookmarklink.setHREF(s1);
    }
    
	protected void updateFromTypeFilter(int iFilterIndex)
    {
		// set filter mode to view all versions
    	super.updateFromTypeFilter(3);
    	//super.updateFromTypeFilter(iFilterIndex);
    }

    protected static final String PARAM_FOLDERIDS = "folderIds";
}
