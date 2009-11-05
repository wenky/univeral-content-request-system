package org.webgen.core;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

// mostly just a thin wrapper around the commons class with abbreviated method names

public class WebgenLogger
{
	private Log logger;
	public boolean tOn, dOn, iOn, wOn;
	public String usersessionkey = "WebgenUser";
	public WebgenLogger(Log l)
	{
		this.logger = l;
		tOn = logger.isTraceEnabled();
		dOn = logger.isDebugEnabled();
		iOn = logger.isInfoEnabled();
		wOn = logger.isWarnEnabled();
	}
	
	// these are truncated versions of the commons log methods
	// - eases placement of log statements on a single ling
	// - allows log statements to double as comments, since they
	//   are more visible in the code, i.e. instead of:
	//      /*-DBG-*/if (log.isDebugEnabled()) log.debug(message);
	//      /*-DBG-*/if (log.dOn) log.dbg(message);
	//   may not seem like much, but it helps, especially on the typing

	// logging commons levels:
	// TRACE DEBUG INFO WARN ERROR FATAL	
	public void trc(String msg){logger.trace(msg);}
	public void trc(String msg, Throwable t){logger.trace(msg, t);}
	public void dbg(String msg){logger.debug(msg);}
	public void dbg(String msg, Throwable t){logger.debug(msg, t);}
	public void inf(String msg){logger.info(msg);}
	public void inf(String msg, Throwable t){logger.info(msg, t);}
	public void wrn(String msg){logger.warn(msg);}
	public void wrn(String msg, Throwable t){logger.warn(msg, t);}
	public void err(String msg){logger.error(msg);}
	public void err(String msg, Throwable t){logger.error(msg, t);}
	public void ftl(String msg){logger.fatal(msg);}
	public void ftl(String msg, Throwable t){logger.fatal(msg, t);}

	// allrighty! let's enable some more powerful webgen logging stuff
	// - get current user from httprequest/session (assumes user info stored in WebgenUser object or descendant)
	// - auto-discovery of resource bundles
	String addUser(HttpServletRequest req, String msg)
	{
		// try to get the user object from the session:
		WebgenUser user = null;
		HttpSession sess = req.getSession(false);
		if (sess != null) user = (WebgenUser)sess.getAttribute(usersessionkey);
		if (user != null) return("(user=["+user.getUser()+"])"+msg);
		else return msg;		
	}	
	public void trc(String msg, Throwable t, HttpServletRequest req) {trc(addUser(req,msg),t);}
	public void dbg(String msg, Throwable t, HttpServletRequest req) {dbg(addUser(req,msg),t);}
	public void inf(String msg, Throwable t, HttpServletRequest req) {inf(addUser(req,msg),t);}
	public void wrn(String msg, Throwable t, HttpServletRequest req) {wrn(addUser(req,msg),t);}
	public void err(String msg, Throwable t, HttpServletRequest req) {err(addUser(req,msg),t);}
	public void ftl(String msg, Throwable t, HttpServletRequest req) {ftl(addUser(req,msg),t);}
}
