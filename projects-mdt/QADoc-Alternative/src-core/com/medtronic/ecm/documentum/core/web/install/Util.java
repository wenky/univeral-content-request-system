package com.medtronic.ecm.documentum.core.web.install;

import java.util.List;
import java.util.Map;

public class Util {
    public static List stmsg(Map status) { return (List)status.get("messages"); }
    public static void msg(String msg, Map status) { List msgs = stmsg(status); msgs.add(msg); }
    public static void msg(Map status, String msg) { List msgs = stmsg(status); msgs.add(msg); }
}
