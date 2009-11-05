package org.tarantula.component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.tarantula.component.interfacedefinition.PageComponentInterface;


/*
 * load from file
 * load from map
 * literal
 * load from httpparams
 */

public class VelocitySubstitute implements PageComponentInterface {

    public String getNickname() { return "VelocitySubstitute"; }

	public void init() throws Exception {
        Velocity.init();
	}

	public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
	{
		// prep velocitycontext using scratchpad
		VelocityContext context = new VelocityContext(scratchpad);

		StringWriter output = new StringWriter();
		
		// get template from arguments
        String template = (String)arguments.get("Template");
                
		boolean result = Velocity.evaluate(context,output, "TarantulaTemplate", new StringReader(template));

		return output.toString();
	}

}
