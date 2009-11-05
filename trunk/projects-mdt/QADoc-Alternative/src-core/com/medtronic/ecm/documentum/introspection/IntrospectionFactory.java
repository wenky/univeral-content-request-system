package com.medtronic.ecm.documentum.introspection;

import com.medtronic.ecm.documentum.introspection.config.GenericConfigFactory;
import com.medtronic.ecm.documentum.introspection.config.MrcsApplication;
import com.medtronic.ecm.documentum.introspection.config.MrcsConfigBroker;
import com.medtronic.ecm.documentum.introspection.config.MrcsDocumentType;
import com.medtronic.ecm.documentum.introspection.config.MrcsGroupingFolderType;
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
