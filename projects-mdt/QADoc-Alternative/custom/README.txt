This custom application folder is provided for your custom application. Do not modify the JSP pages, XML definitions, or other resource files in the WDK and Webcomponent application folders. 

You must register the top-level virtual application folder (this folder, or some other custom folder) in theWeb application deployment descriptor file, /WDB-INF/web.xml. Change the AppFolderName to the name of your top-level application folder. For example:
  
 1. edit the following in the file "R:\WDKApps_5.3_Build5\app\WEB-INF\web.xml"

   <context-param>
      <param-name>AppFolderName</param-name>
      <param-value>custom</param-value>         
   </context-param>
