package org.webgen.util;

import org.hibernate.Session;

public class HibernateSave {

	public static void exec(Session hibernatesession, Object bean) 
	{
    	hibernatesession.saveOrUpdate(bean);
	}

}
