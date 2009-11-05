package com.medtronic.ecm.documentum.core.lifecycle;

import com.documentum.fc.client.IDfBusinessObject;
import com.documentum.fc.client.IDfModule;
import com.documentum.fc.client.IDfService;
import com.documentum.fc.lifecycle.IDfLifecycleUserAction;
import com.documentum.fc.lifecycle.IDfLifecycleUserPostProcessing;

public interface IMdtConfigurableLifecycleAction extends IDfService,
														 IDfModule,
														 IDfBusinessObject,
														 IDfLifecycleUserPostProcessing, 
														 IDfLifecycleUserAction{

}
