import javax.servlet.http.HttpServletRequest;
import com.cem.lweb.core.ThreadData;
	
HttpServletRequest request = ThreadData.getHttpRequest();
request.setAttribute("GroovyWasHere", "Hello groovy world");
