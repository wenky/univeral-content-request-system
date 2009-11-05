package org.tarantula.component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.tarantula.component.interfacedefinition.PageComponentInterface;

// transforms XML using Velocity/anakia

public class VelocityTransform implements PageComponentInterface {

    public String getNickname() { return "VelocityTransform"; }

    public void init() throws Exception {
        Velocity.init();
    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String xmldoc = (String)arguments.get("XmlInput");        
        String template = (String)arguments.get("Template");        
        
        SAXBuilder builder;
        Document root = null;

        builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser" );
        root = builder.build(xmldoc);
        
        // prep velocitycontext using scratchpad
        VelocityContext context = new VelocityContext(scratchpad);
        context.put("root",root);

        StringWriter output = new StringWriter();
        boolean result = Velocity.evaluate(context,output, null, new StringReader(template));

        return output.toString();
    }

}
