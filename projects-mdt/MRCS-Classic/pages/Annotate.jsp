<%--
***********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: Annotate.jsp,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2006/03/13 19:07:26 $

***********************************************************************
--%>


<%@ page language="java" import="java.util.*" %>
<%@ taglib uri="/WEB-INF/tlds/dmform_1_0.tld" prefix="dmf" %>
<%@ taglib uri="/WEB-INF/tlds/dmformext_1_0.tld" prefix="dmfx" %>


<%
    // NOTE: the container/component objects don't get instantiated because there isn't any DCTM taglibs employed by this view.
    //       Apparently, the component launcher goes to the default view.jsp, and that JSP presumably has the DCTM WDK tag that
    //       activates/instantiates the component and component container objects that utilize this view.
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

    // Set to expire far in the past.
    response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");

    // Set standard HTTP/1.1 no-cache headers. 
    response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

    // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
    response.addHeader("Cache-Control", "post-check=0, pre-check=0");
    
    // Set standard HTTP/1.0 no-cache header.
    response.setHeader("Pragma", "no-cache");

    String s = request.getParameter("objectId");    
    String mrcsapp = (String)request.getSession().getAttribute("annotate_mrcs_app");
    String newWinURL = request.getContextPath() + "/PdfAnnotate/thepdf.pdf?objectId="+s+"&mrcsapp="+mrcsapp+"&junk=junk.pdf"; 
    String useragent = request.getHeader("User-Agent");

%>
  <SCRIPT language="JavaScript1.2">
      function mypopup()
         {
            myWindow = window.open("<%=newWinURL%>",  "mywindow","menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes,width=800,height=600") 
            //myWindow.document.write("Welcome to this new window!")
            //myWindow.document.bgColor="lightblue"
            //myWindow.document.close() 
            //onload="javascript: mypopup()"
        } 
  </SCRIPT>
  <head>
    <base href="<%=basePath%>">    
    <title>My JSP 'Annotate.jsp' starting page</title>    
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content=<%= new java.util.Date() %>>
    <meta http-equiv="no-cache"> 
    <!--
    <link rel="stylesheet" type="text/css" href="styles.css">
    -->
    <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
  </head>
  <%
        //if (useragent.indexOf("MSIE") != -1)
        {
            //this is...Internet Exploder, back button works in IE so we don't need to do parent.window.refresh like Firefox
            %> <!-- html><body><embed src="<%=newWinURL%>" width="100%" height="100%" type="application/pdf" fullscreen="yes" /></body></html --> <%
        }
        //else 
        {
            // Mozilla, Firefox, or a similarly sane browser...
            // mypopup puts in in a new window, parent.location.reload auto-returns from the pdf launch jsp (this jsp) page it was just on
            %> 
                <html><body onLoad="mypopup(); parent.location.reload()"></body></html>
            <%
        }            
  %>  
