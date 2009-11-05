package com.liteserv.forms;

import java.util.List;

// - field-specific settings for rendering, validating, grouping, and formatting a form widget
// - these are manufactured by the entitytypes implementation classes, so DCTM would use the DCTM data dictionary

public class FormField 
{
    public boolean isKey;               // field is primary key (almost always read-only)
    public boolean isVisible;           // for hidden fields, for whatever reason (javascript needs it, etc)
    public boolean isRequired;          // required validation, indicate required field...
    public boolean isEnabled;           // widget is enabled initially
    /* TODO: rules/settings to conditionally enable fields */
    public String Name;                 // entity field mapping
    public String Label;                // display label
    public String Help;                 // Tooltip/help text/resource/etc
    public String DefaultValue;         // for Create-mode forms
    public String Widget;               // text, longtext, date, datetime, combobox/dropdown, radio, checkbox,
    public String Datatype;             // bigint, string, boolean, long, double, time, date, datetime, image, blob, clob, etc
    public String InputMask;            // formatting/regexp mask to enforce or validate
    public String Group;                // group/subgroup widget in a tab/box/etc. need more?
    public String Computation;          // Javascript-side computation expression (field should be read-only I'd guess)
    public List ClientValidations;      // javascript/client-executed validations (simple)
    public List ServerValidations;      // server validations (more complicated)
    public boolean hasValueAssistance;
    public List CodeValues;             // dropdown value list (codes), for radio buttons/ checkbox sets/ comboboxes
    public List DisplayValues;          // dropdown display list (list of string arrays for multicolumn display values, or simple list of strings for single display values)
}
