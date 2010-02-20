framework-agnostic code

ideally, this base code should be easily adapted to various execution environments:

- Spring
- Guice
- J2EE/JBoss/Websphere
- Tomcat/simple wars

Basic Process:

A servlet registered in web.xml wraps a ContentRequestServer bean.

+ The ContentRequestServer bean holds a list of ContentRequestHandler beans. 

+ Each ContentRequestHandler bean is is invoked in order with the HttpServletRequest until one returns true indicating the request was handled successfully:

++  A ContentRequestHandler bean typically wraps a ContentRequestParser and a ContentSource.

+++   The ContentRequestParser examines the HttpServletRequest and forms a ContentRequest object out of the url, http parameters, cookies, headers, etc.

+++   The ContentSource is invoked with the ContentRequest produced by the ContentRequestParser, and returns a ContentResponse

+++   If ContentResponse.isFound() is true, then the ContentResponse's inputstream is outputted to the response and the ContentRequestHandler returns true. Otherwise it returns false.
       

 