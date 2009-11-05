package org.webgen.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.webgen.core.WebgenLogger;

public class HQLUpdate
{
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(HQLQuery.class));		

	
	public static int exec(Session hibernatesession, String hqlupdate, Map context) 
	{
		/*-CFG-*/String m="exec(sess,str,map)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"compiling query "+hqlupdate);
    	// prepare as need be
    	Query qry = hibernatesession.createQuery(hqlupdate);
    	// faster way? 
		/*-TRC-*/if(log.tOn)log.trc(m+"getting named parameter list");
    	String[] named = qry.getNamedParameters();
    	for (int i=0; i < named.length; i++)
    	{
    		// assign named parameter
    		/*-TRC-*/if(log.tOn)log.trc(m+"setting parameter "+named[i]);
    		qry.setParameter(named[i],context.get(named[i]));
    	}
		/*-TRC-*/if(log.tOn)log.trc(m+"executing update");
    	int updatecount = qry.executeUpdate();
		/*-TRC-*/if(log.tOn)log.trc(m+"number of beans updated: "+updatecount);
    	return updatecount;
	}
	
}
