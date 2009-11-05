package com.medtronic.ecm.documentum.util.scs.iceobjects;

import java.util.List;

public class IcePayload {
    public String PayloadId;
    public String Timestamp;
    public String Version;
    public List<IceHeader> Headers;
    public IceRequest Request;
}
