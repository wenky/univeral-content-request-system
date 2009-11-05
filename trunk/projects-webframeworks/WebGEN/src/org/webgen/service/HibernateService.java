package org.webgen.service;

import java.util.Map;

import javax.servlet.ServletContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

// provides hibernate service (typically placed in the ServletContext attrs)

public class HibernateService 
{
    private SessionFactory sessionFactory;
    private ThreadLocal session;    
    private Map initconfig; 

    public HibernateService()
    {
        // Create the SessionFactory
        sessionFactory = new Configuration().configure().buildSessionFactory();
        session = new ThreadLocal();
    }
    
    public Session currentSession() {
    	Session s;
    	// get session for current thread...if one is active
        if ((s = (Session)session.get()) == null) {
        	// no active session for this thread, so set one up
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    public void closeSession() {
        Session s = (Session) session.get();
        if (s != null)
            s.close();
        session.set(null);
    }


}
