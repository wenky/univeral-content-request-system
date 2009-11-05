package com.medtronic.documentum.mrcs.introspection;

import com.medtronic.documentum.mrcs.config.GenericConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsApplication;
import com.medtronic.documentum.mrcs.config.MrcsConfigBroker;
import com.medtronic.documentum.mrcs.config.MrcsDocumentType;
import com.medtronic.documentum.mrcs.config.MrcsGroupingFolderType;
import com.thoughtworks.xstream.XStream;

public class IntrospectionFactory extends GenericConfigFactory 
{
    static IntrospectionFactory singletonFolderConfig;


    public static IntrospectionFactory getConfig()
    {

        // our factory method for configbroker
        if (singletonFolderConfig == null) {
            synchronized (IntrospectionFactory.class) {
                singletonFolderConfig = new IntrospectionFactory();
            }
        }
        return singletonFolderConfig;
    }
    
    public String getMrcsApplication(String app) throws Exception
    {
    	MrcsApplication mrcsapp = this.getApplication(app);
		XStream prepped = MrcsConfigBroker.getConfigBroker().getPreppedXStreamFromDCTM();
		String dump = prepped.toXML(mrcsapp);
		return dump;
    }

    public String getMrcsDocumentType(String app, String type) throws Exception
    {
    	
    	MrcsApplication mrcsapp = this.getApplication(app);
    	MrcsDocumentType doctype = (MrcsDocumentType)mrcsapp.DocumentTypes.get(type);
		XStream prepped = MrcsConfigBroker.getConfigBroker().getPreppedXStreamFromDCTM();
		String dump = prepped.toXML(doctype);
		return dump;
    }

    public String getMrcsFolderType(String app, String type) throws Exception
    {
    	MrcsApplication mrcsapp = this.getApplication(app);
    	MrcsGroupingFolderType foldtype = (MrcsGroupingFolderType)mrcsapp.GroupingFolderTypes.get(type);
		XStream prepped = MrcsConfigBroker.getConfigBroker().getPreppedXStreamFromDCTM();
		String dump = prepped.toXML(foldtype);
		return dump;
    }

}
