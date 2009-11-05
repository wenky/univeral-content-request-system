package org.webgen.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.webgen.core.WebgenLogger;

public class HQLQuery 
{
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(HQLQuery.class));		
	
	public static List exec(Session hibernatesession, String hqlquery, Map context)  
	{
		/*-CFG-*/String m="exec(sess,str,map)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"compiling query "+hqlquery);
    	// prepare as need be
    	Query qry = hibernatesession.createQuery(hqlquery);
    	// faster way? 
		/*-TRC-*/if(log.tOn)log.trc(m+"getting named parameter list for query");
    	String[] named = qry.getNamedParameters();
    	for (int i=0; i < named.length; i++)
    	{
    		// assign named parameter
    		/*-TRC-*/if(log.tOn)log.trc(m+"setting parameter "+named[i]);
    		qry.setParameter(named[i],context.get(named[i]));
    	}
		/*-TRC-*/if(log.tOn)log.trc(m+"getting query result");
    	List result = qry.list();
		/*-TRC-*/if(log.tOn)log.trc(m+"return query result bean list: "+result.size());
    	return result;    	
	}

}
