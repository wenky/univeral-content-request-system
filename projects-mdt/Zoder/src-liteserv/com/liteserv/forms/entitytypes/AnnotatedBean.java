package com.liteserv.forms.entitytypes;

import java.util.List;
import java.util.Map;

import com.liteserv.forms.FormDefinition;
import com.liteserv.forms.IEntityType;

// Hibernate/EJB3 annotated persistence bean
// tries to infer form definitions from 

public class AnnotatedBean implements IEntityType 
{
    public Map configuration;
    
    public FormDefinition getFormDefinition(String type, Map context)
    {
        // entity has entity bean classname: primary settings
        FormDefinition formdef = new FormDefinition();
        // infer key fields from @id annotations
        // infer widget types from inherent field type  
        return formdef;
    }

}
