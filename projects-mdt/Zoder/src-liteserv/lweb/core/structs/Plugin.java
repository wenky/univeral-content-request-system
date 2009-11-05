package lweb.core.structs;

import java.util.List;
import java.util.Map;

public class Plugin {
    public String Class;
    public Map Config;  // static configuration
    public List Args;   // dynamic values to be loaded into context: http parameters, config,context, previous processor output
    public String OutKey; // context key to put the output in, and simple pass-thru the input. If null, then normal output
    public String ErrorAction;
    public String LoadType;     // null == pojo, other means (i.e. DCTM has SBOs...)
}