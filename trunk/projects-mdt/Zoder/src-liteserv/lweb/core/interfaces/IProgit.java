package lweb.core.interfaces;


import java.util.Map;

import lweb.core.ActionContext;

public interface IProgit {
    public String execute(final Map progitdef, ActionContext context, String response);
}
