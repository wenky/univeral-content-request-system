  // render script
  var parameters = {}; // url/request parameters
  var ctx = {};
  getcontent('action-name.subaction','tabledata',ctx);
  getcontent('action-name.subaction','moredata',ctx); // shove away some html
  setdiv()
  substitute('action-name.subaction',ctx); // Javascript templates, maybe?
  jseval('action-name.someextcode',ctx); // eval()'s the returned code (no <script> wrapping tags, right)
