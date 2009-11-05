package com.liteserv.forms;

import java.util.List;

public class FormDefinition 
{
    public String Template;                // specific velocity template/jsp TO 
    public List Fields;                    // list of form fields, ordered in display order
    public List ClientValidations;         // javascript form validations (for complicated multi-field validations)
    public List ServerValidations;         // server-side form validations (for complicated multi-field validations)
}
