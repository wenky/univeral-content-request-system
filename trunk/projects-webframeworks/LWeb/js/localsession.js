  // handleNoSession() is called if current page does not have a sessionid defined
  // - what SHOULD happen in handleNoSession is either a redirect to the login page or
  //   a login div is superimposed
  function LocalSession()
  {
    // methods...
    this.getSessionObject = function(key) 
    {
      var jsontxt = this.getSessionString(key);
      var evalobj = eval('('+jsontxt+')'); 
      return evalobj;
    };
    this.setSessionObject = function (key,value) 
    {
      var jsontxt = $.toJSON(value);
      this.setSessionString(key,jsontxt);
    };
    this.generateSessionKey = function() { return (new Date()).getTime(); };
    this.getSessionString = function(key) { return ""+document.applets["localsession"].getKey(this.sessionkey,key); };
    this.setSessionString = function(key,value) { document.applets["localsession"].setKey(this.sessionkey,key,value); };
    this.initSession = function(user,pass,base) 
    { 
      this.user = user; this.pass = pass; this.base = base; 
      this.sessionkey=this.generateSessionKey(); 
      this.setSessionString("user",user);
      this.setSessionString("pass",pass);
      this.setSessionString("base",base);
    };

    // append session div
    //$("body").append("<div><applet id='localsession' codebase='/webtop/extjs2-introspection/applet' code='com.medtronic.documentum.ajax.LocalSession.class' width=0 height=0 mayscript></applet></div>");  
  
    // get extract session key from window URL
    var sessiontemp = ""+window.location;
    var sessidx = sessiontemp.indexOf('session=');
    if (sessidx == -1)
      this.sessionkey = null;
    else {
      sessiontemp = sessiontemp.substring(sessidx);
      this.sessionkey = sessiontemp.replace(/(.*)session\=([0-9]*)(.*)/,"$2");
    }

    // see if login has occurred yet...
    if (this.sessionkey == null || document.applets["localsession"].getSession(this.sessionkey) == null) { handleNoSession(); return; }
    
    // autoretrieves
    this.user = getSessionString("user"); 
    this.pass = getSessionString("pass"); 
    this.base = getSessionString("base"); 
    
  }
  var localsession = new LocalSession();
