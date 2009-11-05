package com.liteserv.forms;

import java.util.List;
import java.util.Map;

// defines an entitytype that stores the necessary code to load and access configured data 
// dictionary settings for the forms engine 

public interface IEntityType 
{
    public FormDefinition getFormDefinition(String type, Map context);
    
    //public Object createEntity();
    //public Object updateEntity();
    // ?validate calls?
    // delete we leave for another interface. you don't need a form to delete something 
    
}
