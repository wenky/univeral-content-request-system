package com.uhg.umvs.bene.cms.contentretrieval.server.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.uhg.umvs.bene.cms.contentretrieval.server.ContentRequestServer;

/**
 * servlet accessing spring-managed/initialized Content Request Server
 */
public class CRSSpringServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public static final String CRS_BEAN_NAME = "CRS_BEAN_NAME";
       
    ContentRequestServer contentserver = null;
    public void setContentServer(ContentRequestServer contentsrv) { this.contentserver = contentsrv; }

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
        String qstring = req.getQueryString();
        String xpath = req.getPathInfo();
        // assume no modification necessary
        contentserver.processContentRequest(req, resp);
        
        // FS Test:    http://_server_/enterprise-content-directory/POClet?source=CEM-FS&item=doggles.jpg
        // SVN Test:   http://_server_/enterprise-content-directory/POClet?source=CEM-SVN&item=beneficiary-portlet-web/pom.xml
        // FTP Test:   http://_server_/enterprise-content-directory/POClet?source=CEM-FTP&item=HEADER.html
        // JCR Test:   http://_server_/enterprise-content-directory/POClet?source=CEM-JCR&item=com/spaceanddeath/geekgrrl/Bahamut.pdf
        
    }


}
