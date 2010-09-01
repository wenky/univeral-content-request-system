<!-- home/main pages call this to generate/setup overall client-side TSA javascript runtime -->
<%=net.tsa.view.Base.get("client-runtime")%>

<!-- 

    Localsession:

	at our disposal for information storage/localsession:
	- cookies
	- applet (requires java)
	- window.name (2 to 32 MB)
	- session tracking server (make separate from view and data server)
	
	
	Links that either do something locally or launch new tab/window on offclick (initiate a subapp)
	- js techniques: onclick vs off-click new tab/window in a href="" onclick="". How did I do this?
	
	Widgets/Portalization/local div:
	- iframes
	- divs + innerHTML from ajax retrieval
	- explicit frames
	- outer frame + 100% inner frame for actual site.
	- event triggers, queues, models, (i.e. communication between sections/portlets/widgets)
	
	Problems/Concerns:
	- centralized/shared css classes	
	- centralized/shared javascript
	- too many requests
	
	For dynamically downloaded divs, problem is that javascript inside the divs is inconsistently 
	executed (so setting innerHTML doesn't necessarily trigger <script> exec?):
	- Mootools XHR Request object may be able to do this (has a evalResponse option that is defaulted to false)
	
	Fundamental challenge: controller/server/portal proxying moving to browser requires everything done in javascript.
	would be nice if:
	- local tab storage was universal (HTML5)
	- div-local scripts/namespace
	- back button and frames...
	- too much state/globals, not enough widgetization and isolation of parts of the page.
	
	Webflow for forms with localsession (basically what webflow is for):
	- initial form page initializes a new localsession form collection object (a mini-session for the form)
	- each new page dumps info into the localsession as fields are filled out
	- we could pass the formsession id in next-page requests.
	- back button: the big problem...if they back button to a page that still has the formsession id in it, 
	               they're okay, the form will just overwrite the values currently there, and the page loads
	               what is currently there. What about first page? formsession should be initialized before 
	               the first page is generated/displayed. Each form page should have its own entry in the 
	               back button navigation history. 
    - so, user decides to start a new form (clicks on a link that kicks off the page): the link javascript
      starts the formsession and generates the formsessionid, and then sets that in a cookie? localsession
      global? passed as url?
    - formsession = transaction id on backend: can't be executed more than once. formsession id should be acquired
      via ajax call to server?
    - stopping multiple submit button clicks and the back button problem is mostly solved, but not completely
    - rely on stateful ajax data services for form processing to validate formsessions and activity? I think its
      okay to timeout forms. Just not the whole site. Also, scrollable datasets will timeout. It's a reality.

 -->