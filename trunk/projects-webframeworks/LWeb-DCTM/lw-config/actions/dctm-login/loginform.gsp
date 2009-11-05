import com.cem.lweb.core.util.*;

out.write """
<div id="dctm-login">
"""

if ("true".equals(request.getParameter("fail"))) {
  out.write """
  <span><font color="red">Login Attempt Failed</font></span>
  """
}

out.write """
<div id="dctm-loginform">
<script type="text/javascript">

  function setCookie( cookieparams )
  {
    document.cookie = cookieparams.name+"="+escape(cookieparams.value)+
      ((cookieparams.expires) ? ";expires=" + cookieparams.expires : "" ) +
      ((cookieparams.path) ? ";path=" + cookieparams.path : "" ) +
      ((cookieparams.domain) ? ";domain=" + cookieparams.domain : "" ) +
      ((cookieparams.secure ) ? ";secure" : "" );
  }



  function dctmLoginInit(divname) 
  {
    // available docbases
    
    // get docbase list for docbase dropdown/combobox....
    var docbaseListStore = new Ext.data.Store({
      proxy: new Ext.data.HttpProxy({url:'[actions][dctm-login][data][docbaselist]'}),
      reader: new Ext.data.JsonReader(
        { root: 'docbases', totalProperty: 'basecount', id: 'base' }, 
        [ {name: 'docbase', mapping: 'base'}, {name: 'docbasename', mapping: 'name'} ]
      )
    });
    
    var loginwidgets = [
      { fieldLabel: 'Login Name', name: 'user',  allowBlank:false },
      { fieldLabel: 'Password',   name: 'pass' , inputType: 'password'},

"""

// optional passthrough hidden field
String passthruurl = null;

if (!Is.empty(request.getParameter("passthru"))) {
    passthruurl = request.getParameter("passthru");
} else if (!Is.empty(request.getAttribute("passthru"))) {
    passthruurl = request.getAttribute("passthru");    
} else {
    passthruurl = C.get("homeaction");
}

out.write """      
      new Ext.form.ComboBox({      
        fieldLabel: 'Repository', name: 'base',
        displayField:'docbase',
        store: docbaseListStore,
        typeAhead: true,
        forceSelection: true,
        triggerAction: 'all',
        emptyText: 'Select Docbase...',
        selectOnFocus: true        
      })
    ];
        
    // layout form in a panel
    var loginform = new Ext.FormPanel({     
      labelWidth: 75,
      frame: true,
      title: 'DCTM Login',
      bodyStyle:'padding:5px 5px 0',
      width: 350,
      defaults: {width: 230},
      defaultType: 'textfield',           
      items: loginwidgets
    });
    
    var passthru = "${passthruurl}";
    var loginFormSuccess = function(form,action) 
        {
          alert("actionresponse: "+action.result.blah);         
          // set cookies locally...
          if (action.result.success) {
            var formdata = form.getValues();
            setCookie({name:"zzzu",value:formdata.user,path:"${request.getContextPath()}",secure:false});
            setCookie({name:"zzzp",value:formdata.pass,path:"${request.getContextPath()}",secure:false});
            setCookie({name:"zzzb",value:formdata.base,path:"${request.getContextPath()}",secure:false});
            alert("success, redirect to "+passthru);
            // redirect to passthru
            window.location=passthru;
          }
        }

    //loginform.on({actioncomplete: loginFormSuccess});
        
    loginform.addButton('Login', function() { 
                                   this.disable(); 
                                   loginform.getForm().submit({
                                     url:"[actions][dctm-login][execlogin]", 
                                     waitMsg:"Authenticating...",
                                     success: loginFormSuccess
                                   })
                                 }
     );
    loginform.render("dctm-loginform");
    
  }
  divInit("dctm-login",dctmLoginInit,"dctm-login");  

</script>
</div>
</div>
"""
