package com.medtronic.ecm.documentum.monitoring.notifiers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.monitoring.IMonitor;
import com.medtronic.ecm.documentum.monitoring.INotifier;

public class AnonymousJavaMail implements INotifier
{
    public void exec(IMonitor monitor, Map notifierconfig, Map context, Map globalconfig) throws Exception 
    {
        List userlist = (List)notifierconfig.get("UserList");
        String from = (String)notifierconfig.get("From");
        String subject = (String)notifierconfig.get("Subject");
        String body = (String)notifierconfig.get("Body");
        String smtp = (String)notifierconfig.get("SmtpServer");
        for (int i=0; i < userlist.size(); i++) {
            String user = (String)userlist.get(i);
            send(smtp,user,from,subject,body);
        }
    }
    
    public static void send(String smtpServer, String to, String from, String subject, String body)
    {
        try {
            /*-dbg-*/Lg.wrn("get system props");
            Properties props = System.getProperties();
            // -- Attaching to default Session, or we could start a new one --
            /*-dbg-*/Lg.wrn("SET smtp host");
            props.put("mail.smtp.host", smtpServer); // CEM: thread safe? I don't think so...
            /*-dbg-*/Lg.wrn("get javax.mail.Session");
            Session session = Session.getDefaultInstance(props, null);
            // -- Create a new message --
            /*-dbg-*/Lg.wrn("create MIME message");
            Message msg = new MimeMessage(session);
            // -- Set the FROM and TO fields --
            /*-dbg-*/Lg.wrn("set from and to");
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to, false));
            // -- We could include CC recipients too --
            // if (cc != null)
            // msg.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc, false));
            // -- Set the subject and body text --
            /*-dbg-*/Lg.wrn("set subject");
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
            /*-ERROR-*/Lg.err("Error occurred in sending JavaMail message %s %s %s",from,to,smtpServer,ex);
            throw EEx.create("MdtJavaMailSend","Error occurred in sending JavaMail message %s %s %s",from,to,smtpServer,ex);            
        }
    }


}
