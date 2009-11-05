package lweb.core.structs;

// alias as "arg", converter: <arg name="" src="" 

public class Arg {
    public String Name;   // value to store it as in the Context map
    public String Source; // http, context, servletconfig/globals, input, ?another action?, resource, ?literal?
    public String Key; // http parameter name, context key, config key, or global key to lookup, if different than "Name"
    public String Value; // for literal values
    
    // resource
    public String LocalUrl;
    public String Url;
    public String File;
    public String Resource;
}
