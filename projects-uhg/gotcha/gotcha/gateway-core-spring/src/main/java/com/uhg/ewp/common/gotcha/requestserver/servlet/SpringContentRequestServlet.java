package com.uhg.ewp.common.gotcha.requestserver.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.requestserver.idef.ContentRequestServer;


/**
 * servlet accessing spring-managed/initialized Content Request Server
 */
public class SpringContentRequestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public static final String CRS_BEAN_NAME = "CRS_BEAN_NAME";
    String getBeanName() { return CRS_BEAN_NAME; }
       
    ContentRequestServer contentserver = null;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        String springBeanName = config.getInitParameter(CRS_BEAN_NAME);
        ServletContext srvctx = this.getServletContext();
        ApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(srvctx);        
        //ApplicationContext appContext = (ApplicationContext) WebApplicationContextUtils.getWebApplicationContext(getServletConfig().getServletContext());
        contentserver = (ContentRequestServer)appContext.getBean(springBeanName);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        contentserver.processContentRequest(req, resp);
    }


}
