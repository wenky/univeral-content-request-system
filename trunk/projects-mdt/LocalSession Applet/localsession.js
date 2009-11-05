  // assumes a global var called localsessionRedirectURL has been defined
  function LocalSession(redirectURL)
  {
    // get session key from args
    this.sessionkey = (""+window.location).replace(/(.*)session\=([0-9]*)(.*)/,"$2");
    // append session div
    $("body").append("<div><applet id='localsession' codebase='applet' code='com.medtronic.documentum.ajax.LocalSession.class' width=0 height=0 mayscript></applet></div>");
    // see if login has occurred yet...
    if (document.applets["localsession"].getSession(sessionkey) == null) { window.location = redirectURL; return; }
    
    // methods...
    this.generateSessionKey = function() { return (new Date()).getTime(); }
    this.getSessionString = function(key) { return ""+document.applets["localsession"].getKey(this.sessionkey,key); }
    this.setSessionString = function(key,value) { document.applets["localsession"].setKey(this.sessionkey,key,value); }
    this.getSessionObject = function(key) 
    {
      var jsontxt = this.getSessionString(key);
      var evalobj = eval('('+jsontxt+')'); 
      return evalobj;
    }
    this.setSessionObject = function (key,value) 
    {
      var jsontxt = $.toJSON(value);
      this.setSessionString(key,jsontxt);
    }
    
    // autoretrieves
    this.user = getSessionString("user"); 
    this.pass = getSessionString("pass"); 
    this.base = getSessionString("base"); 
    
  }
  var localsession = new LocalSession();
