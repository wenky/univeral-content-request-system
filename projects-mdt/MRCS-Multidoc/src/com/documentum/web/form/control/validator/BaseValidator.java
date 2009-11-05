/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   BaseValidator.java

package com.documentum.web.form.control.validator;

import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Label;

// Referenced classes of package com.documentum.web.form.control.validator:
//            IValidator

public abstract class BaseValidator extends Label
    implements IValidator
{

    public final Control getControlToValidate()
    {
        return m_controlToValidate;
    }

    public final void setControlToValidate(Control controlToValidate)
    {
        if(controlToValidate == null)
        {
            throw new IllegalArgumentException("controlToValidate is a mandatory parameter");
        } else
        {
            m_controlToValidate = controlToValidate;
            return;
        }
    }

    public final void setControlToValidate(String strControlName)
    {
        Control control = getForm().getControl(strControlName);
        if(control == null)
        {
            throw new IllegalArgumentException("Control " + strControlName + " does not exist");
        } else
        {
            m_controlToValidate = control;
            return;
        }
    }

    public String getErrorMessage()
    {
        return getLabel();
    }

    public void setErrorMessage(String strErrorMessage)
    {
        setLabel(strErrorMessage);
    }

    public final void validate()
    {
        boolean bValid = true;
        if(isEnabled())
            bValid = doValidate();
        setIsValid(bValid);
    }

    protected abstract boolean doValidate();

    public boolean getIsValid()
    {
        return m_bValid;
    }

    protected void setIsValid(boolean bValid)
    {
        m_bValid = bValid;
    }

    protected BaseValidator()
    {
        m_controlToValidate = null;
        m_bValid = true;
        setCssClass("validatorMessageStyle");
    }

    private Control m_controlToValidate;
    private boolean m_bValid;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/web/form/control/validator/BaseValidator.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/