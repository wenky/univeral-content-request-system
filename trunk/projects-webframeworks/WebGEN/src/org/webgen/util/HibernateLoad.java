package org.webgen.util;

import java.io.Serializable;

import org.hibernate.Session;

public class HibernateLoad
{
    public static Object exec(Session hibernatesession, String classname, Serializable beanid) throws Exception 
    {
    	Object bean = hibernatesession.load(Class.forName(classname),beanid);
    	return bean;
    }
}
