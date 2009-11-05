package org.tarantula.service;

import org.hibernate.*;
import org.hibernate.cfg.*;


// provides hibernate service (typically placed in the GlobalConfig map)

public class HibernateService 
{
    private transient SessionFactory sessionFactory;
    private transient ThreadLocal session;
    
Integer xstreaminittest;

    public HibernateService()
    {
    	try {
	        // Create the SessionFactory
	        sessionFactory = new Configuration().configure().buildSessionFactory();
	        session = new ThreadLocal();
	    } catch (Throwable ex) {
	    	// convert to a runtime exception...
	    	throw new NullPointerException ("HibernateService could not start up: "+ex);
	    }    	
    }    

    public Session currentSession() {
    	// lazy initialization to accomodate XStream deserialization
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
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
