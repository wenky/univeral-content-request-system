package com.uhg.ewp.common.gotcha.util;

import javax.activation.MimetypesFileTypeMap;

public class ActivationMimeTypes
{
    
    static MimetypesFileTypeMap mimetypes = new MimetypesFileTypeMap();

    public static String determineMimeType(String filename)
    {
        String mimetype = mimetypes.getContentType(filename);
        return mimetype;
    }    

}
