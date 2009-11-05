package com.medtronic.documentum.mrcs.client;

import java.util.Iterator;

import javax.servlet.ServletRequest;

import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.session.Login;
import com.documentum.web.formext.session.SessionManagerHttpBinding;


public class PRLAnonymousLogin extends Login implements IReturnListener
{

	public void onInit(ArgumentList argumentlist)
	{
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLAnonymousLogin.onInit - top", null, null);
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this)){Iterator argiter = argumentlist.nameIterator();while (argiter.hasNext()) {String argname = (String)argiter.next();String argval = argumentlist.get(argname);DfLogger.debug(this, "MRCS:PRLAnonymousLogin.onInit - arg: "+argname + " - "+argval, null, null);}}
		String startcomponent = getStartComponent(argumentlist);
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLAnonymousLogin.onInit - startcomponent: "+startcomponent, null, null);
		
		
		//call parent's onInit method first
		super.onInit(argumentlist);

		if ("prlobjectlist".equals(startcomponent)) {
			/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLAnonymousLogin.onInit - PRL-specific startcomponent", null, null);
			try {
				getPRLUserPasswordDocbaseInformation(argumentlist);
				authenticate(m_strDocbase, m_strUsername, m_password ,null);
				SessionManagerHttpBinding.setClientDocbase(m_strDocbase); //CEM: <--needed this to fix NPEs
				setStartPoint();
			} catch(Exception exception) {
				setErrorMessage(exception.getMessage());
			}
		}

	} //End of onInit Method
	
	String getStartComponent(ArgumentList args)
    {
        String strComponentName = args.get("startComponent");
        if(strComponentName == null || strComponentName.length() == 0)
        {
            ServletRequest request = getPageContext().getRequest();
            strComponentName = (String)request.getAttribute("startComponent");
            if(strComponentName == null || strComponentName.length() == 0)
                strComponentName = request.getParameter("startComponent");
            args.add("startComponent", strComponentName);
        }
        return strComponentName;
    }


		
    public void getPRLUserPasswordDocbaseInformation(ArgumentList argumentlist) throws Exception
    {

		IConfigElement  configUserid = lookupElement("prl-userid");
		m_strUsername = configUserid.getValue();
		IConfigElement  configPassword = lookupElement("prl-password");
		m_password = configPassword.getValue();
		IConfigElement  configDocbasename = lookupElement("prl-docbase");
		m_strDocbase =  configDocbasename.getValue();
    
	    String reqDocbase = argumentlist.get("docbaseName");
	    if ((reqDocbase != null) && (reqDocbase.trim().length()>0) && !reqDocbase.equalsIgnoreCase("null"))
	    	m_strDocbase = reqDocbase;
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PRLAnonymousLogin.getPRLUserPasswordDocbaseInformation - "+m_strUsername + "   "+m_strDocbase,null,null);
    } //End of Method getUserPasswordDocbaseInformation

	private String m_strUsername;
    private String m_password;
    private String m_strDocbase;


}//End of Class AnonymousLogin
