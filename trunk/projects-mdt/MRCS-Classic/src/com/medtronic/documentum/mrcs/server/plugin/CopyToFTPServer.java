/*
 * Created on October 15, 2006
 *
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: CopyToFTPServer.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2007/02/02 22:25:30 $

***********************************************************************
*/
package com.medtronic.documentum.mrcs.server.plugin;

import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfException;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;
import com.documentum.fc.client.IDfFormat;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

public class CopyToFTPServer implements IMrcsLifecyclePlugin {

	public void execute(IDfSessionManager sMgr, String docbase,
			MrcsLifecycleState targetstate, String mrcsapp,
			IDfSysObject mrcsdocument, Map config, Map context) {

		try {

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - Entry of CopyToFTPServer : " , null,null);
	        FTPClient ftp = new FTPClient();

	        FTPClientConfig ftpConfig = new FTPClientConfig();

	        ftpConfig.setServerLanguageCode((String)config.get("LanguageCode"));

	        int reply;

	        ftp.configure(ftpConfig);

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - connect to FTP Server : " , null,null);
	        ftp.connect((String)config.get("CopyToServer"));

	        reply = ftp.getReplyCode();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - FTP Server reply code: " + reply , null,null);

	        if(!FTPReply.isPositiveCompletion(reply)) {
	            ftp.disconnect();
	            DfLogger.error(this,"FTP server refused connection.",null,null);
	          }

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - logging into FTP server with user: " + config.get("user") , null,null);
	        if (!ftp.login((String)config.get("user"), (String)config.get("password"))){
	        	ftp.disconnect();
	            DfLogger.error(this,"FTP server refused login credentials.",null,null);
	        }

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - change directory on FTP server: " + config.get("CopyToFolder") , null,null);
	        reply = ftp.cwd((String)config.get("CopyToFolder"));
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - FTP Server reply code: " + reply , null,null);

	        if(!FTPReply.isPositiveCompletion(reply)) {
	            ftp.disconnect();
	            DfLogger.warn(this,"FTP server failed to change directories.",null,null);
	        }

	        //construct the filename/content stream for the transferred file
	        //get the preferred format for the content.
	        String preferredFormat = (String)config.get("Format");
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - Configured format: " + preferredFormat , null,null);
	        InputStream content = null;
	        String filename = null;
	        String extension = null;

	        //retrieve the content for the preferred format
	        if (!(preferredFormat.equalsIgnoreCase("native"))){
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch format: " + preferredFormat , null,null);
	        	IDfFormat format = mrcsdocument.getSession().getFormat(preferredFormat);
	        	extension = format.getDOSExtension();
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch format extension: " + extension, null,null);
	        	content = mrcsdocument.getContentEx(preferredFormat,0);
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch content: " + preferredFormat , null,null);
	        } else {
	        	extension = mrcsdocument.getFormat().getDOSExtension();
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch format extension: " + extension, null,null);
	        	content = mrcsdocument.getContent();
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch content: " + preferredFormat , null,null);
	        }

	        String objectName = mrcsdocument.getObjectName();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - fetch object name: " + objectName , null,null);
	        byte[] utfByteObjectName = objectName.getBytes("UTF-8");
	        String utfObjectName = new String(utfByteObjectName, "UTF-8");
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - utf object name: " + utfObjectName , null,null);
	        byte[] utfByteExtention = new String("." + extension).getBytes("UTF-8");
	        String utfExtension = new String(utfByteExtention, "UTF-8");
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - utf extension: " + utfExtension , null,null);
        	filename = utfObjectName + utfExtension;
        	byte[] utfFilename = filename.getBytes("UTF-8");
        	filename = new String(utfFilename, "UTF-8");
        	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - construct filename: " + filename , null,null);

	        if (!ftp.storeFile(filename, content)){
	        	DfLogger.warn(this,"FTP server did not store new file.",null,null);
	        }

	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " - disconnect: ", null,null);
	        if (ftp.isConnected()) {
	        	ftp.disconnect();
	        }

		} catch (IOException ioe){
			DfLogger.error(this,"There was an IO exception.",null,ioe);

		} catch (DfException dfe) {
			DfLogger.error(this,"FTP server could not store file from Documentum.",null,dfe);

		}

	}
}
