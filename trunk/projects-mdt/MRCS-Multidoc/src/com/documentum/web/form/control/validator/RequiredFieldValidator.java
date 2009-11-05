/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   RequiredFieldValidator.java

package com.documentum.web.form.control.validator;

import com.documentum.web.form.control.*;

// Referenced classes of package com.documentum.web.form.control.validator:
//            BaseValidator

public class RequiredFieldValidator extends BaseValidator
{

    public RequiredFieldValidator()
    {
        m_strIndicator = " *";
        m_strIndicatorStyle = null;
        m_strIndicatorCssClass = "requiredFieldAsterisk";
    }

    public void setIndicator(String strIndicator)
    {
        m_strIndicator = strIndicator;
    }

    public String getIndicator()
    {
        return m_strIndicator;
    }

    public void setIndicatorStyle(String strIndicatorStyle)
    {
        m_strIndicatorStyle = strIndicatorStyle;
    }

    public String getIndicatorStyle()
    {
        return m_strIndicatorStyle;
    }

    public void setIndicatorCssClass(String strIndicatorCssClass)
    {
        m_strIndicatorCssClass = strIndicatorCssClass;
    }

    public String getIndicatorCssClass()
    {
        return m_strIndicatorCssClass;
    }

    protected boolean doValidate()
    {
        boolean bValid = false;
        String strValue = null;
        com.documentum.web.form.Control controlToValidate = getControlToValidate();
        if(controlToValidate instanceof DateInput)
        {
            DateInput dateInput = (DateInput)controlToValidate;
            java.util.Date date = dateInput.toDate();
            if(date != null)
                bValid = true;
        } else
        if(controlToValidate instanceof StringInputControl)
        {
            strValue = ((StringInputControl)controlToValidate).getValue();
            if(strValue != null && strValue.trim().length() > 0)
                bValid = true;
        } else
        if(controlToValidate instanceof Label)
        {
            strValue = ((Label)controlToValidate).getLabel();
            if(strValue != null && strValue.trim().length() > 0)
                bValid = true;
        } else
        if(controlToValidate instanceof BooleanInputControl)
            bValid = true;
        return bValid;
    }

    private static final String DEFAULT_INDICATOR = " *";
    private static final String DEFAULT_INDICATOR_CSSCLASS = "requiredFieldAsterisk";
    private String m_strIndicator;
    private String m_strIndicatorStyle;
    private String m_strIndicatorCssClass;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/web/form/control/validator/RequiredFieldValidator.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/