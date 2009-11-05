import org.webgen.service.*;
// set up services and stuff in here
// servlet context is bound to "application" and "context"

// initialize Hibernate service
context.setAttribute("Hibernate",new HibernateService());
//context["Hibernate"] = new HibernateService();
