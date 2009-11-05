package com.medtronic.documentum.mrcs.client;

import org.apache.log4j.*;
import java.io.*;
import com.documentum.web.formext.component.Component;
import com.documentum.web.form.control.Label;
import com.documentum.web.common.ArgumentList;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfACL;
import com.documentum.web.common.WrapperRuntimeException;
import java.util.Calendar;

public class LogReader extends Component {

	private String logfile;
	private String logpath;

	public void onInit(ArgumentList args) {

		logfile = "MRCS LOG " + Calendar.getInstance().getTime().toString();
		logpath = "/System/Applications/mrcs_core/logs";

		try {
			//I picked the client package to get the appender
			Logger log = 	Logger.getLogger("com.medtronic.documentum.mrcs.client");
			Appender app = log.getAppender("MRCSAppender");
			FileAppender fapp = (FileAppender) app;
			Label lblLog = (Label) getControl("log", Label.class);

			//get the session from this component
			IDfSession sess = getDfSession();

			//create a new content object
		    IDfSysObject sysObj = (IDfSysObject)sess.newObject("dm_document");
		    sysObj.setObjectName(logfile);
		    sysObj.setContentType("crtext");

		    //transfer the file to a bytearray stream for setting the content
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(fapp.getFile());
			int count;
            byte[] b = new byte[512];
            while ((count = fis.read(b)) > 0) {
            	out.write(b, 0, count);
            }
            out.close();
            fis.close();

            sysObj.setContent(out);
		    sysObj.link(logpath);
		    IDfACL logACL = sess.getACL("mrcs_main", "dm_acl_systemmethod");
		    sysObj.setACL(logACL);
            sysObj.save();

			lblLog.setLabel(sess.getLoginUserName()  + " created logfile: " + logfile + " at: " + logpath);
		}
		catch (Exception e) {
			throw new WrapperRuntimeException("There was a problem creating the log file.", e);
		}

	}
}
