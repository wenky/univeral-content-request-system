package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.Iterator;

import com.documentum.web.common.ArgumentList;
import com.documentum.webcomponent.library.create.NewDocContainer;
import com.medtronic.ecm.common.Lg;

public class MdtNewApprovalFormContainer extends NewDocContainer 
{
    public void onInit(ArgumentList arg) {
        /*-INFO-*/Lg.inf("Top)");
        Iterator i = arg.nameIterator(); 
        /*-dbg-*/if(Lg.dbg())while (i.hasNext()) { String argname = (String)i.next();Lg.dbg(argname + " : " + arg.get(argname));}
        
        // analyze args to determine if no-select, single select, or multiselect
        // the multi/single select action sets the "objectIds" param/arg to "multi" as a flag. actual list of args is in the componentArgs...
        if ("multi".equals(arg.get("objectIds"))) {
            // futz with the arguments...yes, this is "hacky"
            /*-dbg-*/Lg.dbg("MULTI-call");
            String folderid = arg.get("folderId");
            arg.replace("objectId",folderid);
        } else {
            /*-dbg-*/Lg.dbg("empty call");
            /*do nothing*/
        }

        /*-INFO-*/Lg.inf("call NewDocContainer.onInit");
        super.onInit(arg);
        setEditAfterCreation(false);
        
        // HMMM..., but if you checkout and cancel checkout, it still deletes 
        // the doc (since you cancelled a checkout of a new doc), maybe we should
        // checkout...checkin the document by decorating the super.onOk call, would
        // that un-New it? Think so...
        
        

    }
    


}
