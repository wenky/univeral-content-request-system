
import com.documentum.ucf.server.transport.ICommManager;
import com.documentum.web.common.*;
import com.documentum.web.contentxfer.ContentTransferConfig;
import com.documentum.web.contentxfer.Trace;
import com.documentum.web.form.*;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.ComponentDef;
import com.documentum.web.formext.config.*;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;


// this class handles:
// - management of an instance of the UcfSessionManager in the HttpSession...


public class NewUcfTransportManager
    implements IUcfTransportManagerConstants
{
	
	// wraps the UcfSessionManager with an servlet httpsession listener that shuts it down when the httpsession expires. 
    protected static class UcfSessionStore implements HttpSessionBindingListener, Serializable
    {
        private transient NewUcfSessionManager m_sessionManager;

        protected NewUcfSessionStore() {} // no public instantiation of the Store object...

        public NewUcfSessionManager getSessionManager()
        {
            if(m_sessionManager == null)
                m_sessionManager = new NewUcfSessionManager(ContentTransferConfig.getConfig().getServerContentLocation()); // may need to reimplment the config lookup (ContentTransferConfig)
            return m_sessionManager;
        }

        public void valueUnbound(HttpSessionBindingEvent arg0)
        {
            if(m_sessionManager != null)
                m_sessionManager.shutdown();
        }

        public void valueBound(HttpSessionBindingEvent httpsessionbindingevent)
        {
        }

    }


    protected NewUcfTransportManager()
    {
    }
    
    // CommMgr and Locale? Necessary?

    public UcfSessionManager getSessionManager()
    {
        UcfSessionManager mgr = getStore().getSessionManager();
        if(mgr.getCommMgr() == null)
        {
            ICommManager commMgr = (ICommManager)SessionState.getAttribute("com.documentum.ucf");
            if(commMgr != null)
                mgr.setCommMgr(commMgr);
            mgr.setLocale(LocaleService.getLocale());
        }
        return mgr;
    }


    // ??? might be needed, this corresponds with one of the UcfInit Applet params
    public int getLauncherMode()
    {
        String lookupStr = "application.contentxfer.client.ucf-launch-mode";
        String strmode = ConfigService.getConfigLookup().lookupString(lookupStr, Context.getSessionContext());
        if(strmode != null)
        {
            if(strmode.equals("new_process"))
                return 1;
            if(strmode.equals("shared_process"))
            {
                return 2;
            } else
            {
                Trace.println(this, "unknown ucf-launch-mode value: " + strmode);
                return 1;
            }
        } else
        {
            return 2;
        }
    }

    // ?cookie?
    public String getLauncherCookie()
    {
        return getSessionManager().getLauncherCookie();
    }

    protected UcfSessionStore getStore()
    {
    	// get from HTTPSession...need to recode...
        UcfSessionStore store = (UcfSessionStore)SessionState.getAttribute(STORE_KEY);
        if(store == null)
            synchronized(STORE_KEY)
            {
                store = (UcfSessionStore)SessionState.getAttribute(STORE_KEY);
                if(store == null)
                {
                    store = new UcfSessionStore();
                    SessionState.setAttribute(STORE_KEY, store);
                }
            }
        return store;
    }

    public static UcfTransportManager getManager()
    {
        if(s_instance == null)
            synchronized(com.documentum.web.contentxfer.ucf.UcfTransportManager.class)
            {
                if(s_instance == null)
                {
                    String clName;
                    try
                    {
                        ResourceBundle configBundle = ResourceBundle.getBundle((com.documentum.web.contentxfer.ucf.UcfTransportManager.class).getName());
                        clName = configBundle.getString("managerClass");
                    }
                    catch(MissingResourceException e)
                    {
                        clName = null;
                    }
                    if(clName != null && clName.length() > 0)
                        try
                        {
                            Class cl = Class.forName(clName);
                            s_instance = (UcfTransportManager)cl.newInstance();
                        }
                        catch(ClassNotFoundException e)
                        {
                            throw new WrapperRuntimeException("Failed to initialize manager", e);
                        }
                        catch(InstantiationException e)
                        {
                            throw new WrapperRuntimeException("Failed to initialize manager", e);
                        }
                        catch(IllegalAccessException e)
                        {
                            throw new WrapperRuntimeException("Failed to initialize manager", e);
                        }
                    else
                        s_instance = new UcfTransportManager();
                }
            }
        return s_instance;
    }

    private static UcfTransportManager s_instance;
    private static final String STORE_KEY;

    static 
    {
        STORE_KEY = (com.documentum.web.contentxfer.ucf.UcfTransportManager.class).getName() + ".UcfSessionStore";
    }

}
