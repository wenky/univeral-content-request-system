package lweb.core.interfaces;


import java.util.Map;

import lcontext.Context;

public interface IProgit {
    public String execute(final Map progitdef, Context context, String response);
}
