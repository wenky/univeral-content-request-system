package com.documentum.webcomponent.library.workflow;

import com.documentum.web.form.query.Expression;
import com.documentum.web.form.query.ParsedExpression;
import com.documentum.webcomponent.library.locator.SysObjectLocatorQuery;

public class WorkflowTemplateLocatorQueryHelper
{

    private Expression m_templateFilter;
    private SysObjectLocatorQuery m_parent;
    private String m_viewAlias;

    public WorkflowTemplateLocatorQueryHelper(String viewAlias, SysObjectLocatorQuery parent)
    {
        m_templateFilter = null;
        m_viewAlias = "";
        if(parent == null)
        {
            throw new NullPointerException("Argument cannot be null.");
        }
        m_parent = parent;
        if(viewAlias != null)
        {
            m_viewAlias = viewAlias;
        }
    }

    protected Expression getSystemTemplateFilter()
    {
        if(m_templateFilter == null)
        {
//        	String strClassPath = System.getProperty("java.class.path", ".");
//        	System.out.println("Java Classpath: " + strClassPath);
        	
            String alias = m_viewAlias.length() <= 0 ? "" : m_viewAlias + '.';
            StringBuffer sbuf = new StringBuffer(175);
//          Start of Change
            sbuf.append("NOT (object_name LIKE 'dmSendTo%')");
//            Start of Change
//            SWORD INC.
//            08-01-2006 Modified Where clause for performance reasons
//            sbuf.append("NOT ((FOLDER('/Resources', DESCEND) OR FOLDER('/System')) ").append("OR FOLDER('/System/DistributionList Templates', DESCEND) AND ").append(alias).append("object_name LIKE 'dmSendTo%')");
//            End of Change
            m_templateFilter = new ParsedExpression(sbuf.toString());
        }
        return m_templateFilter;
    }
}
