import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cem.dctm.DctmUtils;
import com.cem.lweb.core.ThreadData;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;

HttpServletRequest request = ThreadData.getHttpRequest();
IDfSessionManager smgr = (IDfSessionManager)request.getAttribute("zzzsmgr");
String base = (String)request.getAttribute("zzzb");
   
String dql = request.getParameter("query");
IDfSession session = null;
try {
    session = smgr.getSession(base);
    List results = DctmUtils.execMultiColumnQuery(session, dql);
    context.put("queryresults",results);
} finally {try{smgr.release(session);}catch(Exception e){}}
