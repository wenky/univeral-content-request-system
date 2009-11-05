

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.cem.lweb.core.ThreadData;
import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfDocbaseMap;


def request = ThreadData.getHttpRequest();
        
IDfClientX clientx = new DfClientX();
IDfDocbaseMap map = clientx.getDocbrokerClient().getDocbaseMap();
List docbaselist = [];
for (int i=0; i < map.getDocbaseCount(); i++)
{
    String curbase = map.getDocbaseName(i);
    docbaselist[i] = curbase;
}

request.setAttribute("docbaselist",docbaselist);
