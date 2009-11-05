package com.medtronic.ecm.documentum.mdtworkflow.method.common;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.util.DctmUtils;
import com.medtronic.ecm.util.Is;

public class MailUtils {

    // resolves DCTM user or group to user's address, or for groups, the addresses of all users in the group
    // including nested groups. If a user is passed in, the user's email is added to the group if valid 
    public static void addDctmGroupToAddressSet(IDfSession session, String entityname, Set addresses) throws DfException
    {
        /*-INFO-*/Lg.inf("get list of addrs for a group or user");
        
        /*-dbg-*/Lg.wrn("lookup user object");
        IDfUser userobj = (IDfUser)session.getUser(entityname);
        if (userobj != null) {
            /*-dbg-*/Lg.wrn("test if user object %s is a group or a user",userobj);
            if (userobj.isGroup())
            {
                /*-dbg-*/Lg.wrn("IS GROUP, get group object");
                IDfGroup group = (IDfGroup)session.getGroup(entityname);
                /*-dbg-*/Lg.wrn("iterate through members of group object %s",group);
                for (int i=0; i < group.getUsersNamesCount(); i++)
                {
                    /*-dbg-*/Lg.wrn("next subuser");
                    String name = group.getUsersNames(i);
                    /*-dbg-*/Lg.wrn("get user object for subusername: %s",name);
                    IDfUser subuser = session.getUser(name);
                    if (subuser != null) {
                        if (subuser.isGroup()) {
                            /*-dbg-*/Lg.wrn("current user is a group, recurse into nested group: %s",name);
                            addDctmGroupToAddressSet(session,name,addresses);
                            /*-dbg-*/Lg.wrn("--nested group/recursive queue call done");
                        } else {
                            /*-dbg-*/Lg.wrn("adding subuser: %s",name);
                            String address = subuser.getUserAddress();
                            /*-dbg-*/Lg.wrn("-- subuser address set: %s",address);
                            addresses.add(address);
                        }                    
                    } else {
                        /*-WARN-*/Lg.wrn("user %s not found during message queueing request",name);                    
                    }
                }
            } else {
                /*-dbg-*/Lg.wrn("adding subuser: %s",entityname);
                String address = userobj.getUserAddress();
                /*-dbg-*/Lg.wrn("-- subuser address set: %s",address);
                addresses.add(address);
            }
        } else {
            if (entityname.indexOf('@') != -1) {
                // hardcoded email addr
                /*-dbg-*/Lg.wrn("hardcoded direct add: %s",entityname);
                addresses.add(entityname);
            }                    
        }
    }
    
    public static void addEntityOrListOfEntitiesToAddressSet(IDfSession session, Object recipiententityobj, Set addresses) throws DfException
    {
        if (recipiententityobj instanceof String) {
            // queue single user or string
            String entity = (String)recipiententityobj;
            MailUtils.addDctmGroupToAddressSet(session, entity, addresses);
        } else if (recipiententityobj instanceof List){
            List entities = (List)recipiententityobj;
            for (int i=0; i < entities.size(); i++) {
                MailUtils.addDctmGroupToAddressSet(session, (String)entities.get(i), addresses);
            }
        }
    }
    
    public static void addWorkflowApproversToAddressSet(IDfWorkitem wi,Set addresses)  throws DfException
    {
        IDfWorkflow wf = (IDfWorkflow)wi.getSession().getObject(wi.getWorkflowId());
        addWorkflowApproversToAddressSet(wf,addresses);
    }
    
    public static void addWorkflowApproversToAddressSet(IDfWorkflow wf,Set addresses)  throws DfException    
    {        
        String dql = "SELECT r_object_id from dmi_workitem where r_workflow_id = '"+wf.getObjectId().getId()+"'";
        List workitemids = DctmUtils.execSingleColumnQuery(wf.getSession(), dql);
        for (int i=0; i < workitemids.size(); i++) {
            String itemid = (String)workitemids.get(i);
            IDfWorkitem item = (IDfWorkitem)wf.getSession().getObject(new DfId(itemid));
            String performer = item.getPerformerName();
            if (!Is.empty(performer)) {
                addDctmGroupToAddressSet(wf.getSession(),performer,addresses);
            }
        }
    }
    
    public static void addSupervisorToAddressSet(IDfWorkitem wi, Set addresses) throws DfException
    {
        /*-dbg-*/Lg.wrn("look up workflow object and get supervisor username");
        IDfWorkflow wf = (IDfWorkflow)wi.getSession().getObject(wi.getWorkflowId());        
        /*-dbg-*/Lg.wrn("  -- wf: %s",wf);
        addSupervisorToAddressSet(wf,addresses);
    }
    public static void addSupervisorToAddressSet(IDfWorkflow wf, Set addresses) throws DfException
    {
        String user = wf.getSupervisorName();
        MailUtils.addDctmGroupToAddressSet(wf.getSession(),user,addresses);        
    }
    
    public static void addAttachmentAttributesToAddressSet(List attachments, Set addresses, Object attributespec) throws DfException
    {
        if (attributespec instanceof String) {
            String attr = (String)attributespec;
            for (int i=0; i < attachments.size(); i++) {                
                IDfSysObject attachment = (IDfSysObject)attachments.get(i);
                addAttributeToAddressSet(attachment, addresses, attr);
            }
        } else if (attributespec instanceof List) {
            List attrlist = (List)attributespec;
            for (int i=0; i < attrlist.size(); i++) {
                String attr = (String)attrlist.get(i);
                for (int j=0; j < attachments.size(); j++) {                
                    IDfSysObject attachment = (IDfSysObject)attachments.get(j);                
                    addAttributeToAddressSet(attachment, addresses, attr);
                }
            }
        }
    }

    public static void addAttributesToAddressSet(IDfSysObject idfsysobject, Set addresses, Object attributespec) throws DfException
    {
        if (attributespec instanceof String) {
            String attr = (String)attributespec;
            addAttributeToAddressSet(idfsysobject, addresses, attr);
        } else if (attributespec instanceof List) {
            List attrlist = (List)attributespec;
            for (int i=0; i < attrlist.size(); i++) {
                String attr = (String)attrlist.get(i);
                addAttributeToAddressSet(idfsysobject, addresses, attr);
            }
        }
    }

    public static void addAttributeToAddressSet(IDfSysObject sysobject, Set addresses, String attributename) throws DfException
    {
        /*-dbg-*/Lg.wrn("attr: %s",attributename);
        // iterate through attachments, look for attribute, and queue to single or multi-valued string list
        /*-dbg-*/Lg.wrn("current attachment: %s",sysobject);
        if (sysobject.hasAttr(attributename)) {
            /*-dbg-*/Lg.wrn("has attr");
            if (sysobject.getAttr(sysobject.findAttrIndex(attributename)).isRepeating()) {
                /*-dbg-*/Lg.wrn("iterating repeating list");
                for (int u=0; u < sysobject.getValueCount(attributename); u++)
                {
                    String attruser = sysobject.getRepeatingString(attributename,u);
                    /*-dbg-*/Lg.wrn("queue to %s",attruser);
                    addDctmGroupToAddressSet(sysobject.getSession(),attruser,addresses);
                }
            } else {
                String attruser = sysobject.getString(attributename);
                /*-dbg-*/Lg.wrn("queue to %s",attruser);
                addDctmGroupToAddressSet(sysobject.getSession(),attruser,addresses);
            }
        } 
    }
    
    
    // simple text or HTML single part email dispatch
    public static void send(String smtpServer, String to, String from, String subject, String body, String user, String pass)
    {
        class MdtMailAuthentication extends Authenticator {
            private PasswordAuthentication pwauth = null;
            public MdtMailAuthentication(String user, String pass) { pwauth = new PasswordAuthentication(user,pass); }
            protected PasswordAuthentication getPasswordAuthentication() { return pwauth; }
        }
        
        try {
            /*-dbg-*/Lg.wrn("get system props");
            Properties props = System.getProperties();
            /*-dbg-*/Lg.wrn("SET smtp host to %s",smtpServer);
            props.put("mail.smtp.host", smtpServer);
            // ?mail.smtp.auth property to true? --> investigate/test
            /*-dbg-*/Lg.wrn("authentication connection");
            Authenticator mailauth = new MdtMailAuthentication(user,pass);
            /*-dbg-*/Lg.wrn("get javax.mail.Session");
            Session session = Session.getDefaultInstance(props, mailauth);
            // -- Create a new message --
            /*-dbg-*/Lg.wrn("create MIME message");
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            /*-dbg-*/Lg.wrn("set from: %s",from);
            msg.setFrom(new InternetAddress(from));
            /*-dbg-*/Lg.wrn("set to: %s",to);
            msg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
            // -- Set the subject and body text --
            /*-dbg-*/Lg.wrn("set subject %s",subject);
            msg.setSubject(subject);
            /*-dbg-*/Lg.wrn("set body");
            msg.setContent(body,"text/html");
            // -- Set some other header information --
            /*-dbg-*/Lg.wrn("set header and senddate");
            msg.setHeader("X-Mailer", "MdtDCTMEmail");
            msg.setSentDate(new Date());
            // -- Send the message --
            /*-dbg-*/Lg.wrn("send message");
            Transport.send(msg);
            /*-dbg-*/Lg.wrn("send complete");
        } catch (Exception ex) {
            /*-WARN-*/Lg.wrn("Error occurred in sending JavaMail message %s %s %s",from,to,smtpServer,ex);
            //throw EEx.create("MdtJavaMailSend","Error occurred in sending JavaMail message %s %s %s",from,to,smtpServer,ex);            
        }
    }

    public static void sendToAddresses(String smtpServer, Set addresses, String from, String subject, String body, String user, String pass)
    {
        Iterator i = addresses.iterator();
        while (i.hasNext()) {
            String address = (String)i.next();
            send(smtpServer,address,from,subject,body,user,pass);
        }
    }

    public static String getSmtpServer(IDfSession session) throws DfException
    {
        // try to get it from server config
        IDfTypedObject serverconfig = session.getServerConfig();
        String smtpServer = serverconfig.getString("smtp_server");
        if (Is.empty(smtpServer)) {
            // try to get it from system properties
            Properties props = System.getProperties();
            smtpServer = (String)props.get("mail.smtp.host");
        }
        return smtpServer;
        
    }
}
