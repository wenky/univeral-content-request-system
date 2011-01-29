package com.uhg.ovations.portal.partd.simplewebservicetest.partd;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

import com.uhg.ovations.portal.partd.simplewebservicetest.SpringJspTestBase;

public abstract class PartDJspTestBase extends SpringJspTestBase
{
	public boolean canExecute(HttpServletRequest request, HttpServletResponse response, Object context, Writer sw) throws Exception {
		ApplicationContext appContext = (ApplicationContext)context;
		Boolean b = (Boolean)appContext.getBean("integrationJspTestsEnable");
		if (b == null) return false;
		return b.booleanValue();
	}

}
