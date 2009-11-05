package test;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.medtronic.ecm.documentum.introspection.DctmAccess;


public class blahblah {

    public static void amethod(DctmAccess dctmaccess, HttpServletRequest request, Map bindingmap) throws Exception
    {
        
        // script to create MdtConfigurableScheduledTask job...looks good!
        Date dt = new Date();
        Date dt2 = new Date(); dt2.setYear(dt.getYear()+100);
        Date dt3 = new Date(); dt3.setMinutes(dt.getMinutes()+15);

        IDfSession dctmsess = dctmaccess.accessSession();
        IDfPersistentObject newjob = (IDfPersistentObject)dctmsess.newObject("dm_job");
        newjob.setString         ("object_name",             "MdtConfigurableScheduledTask");
        newjob.setString         ("method_name",             "MdtConfigurableScheduledTask");
        newjob.setBoolean        ("pass_standard_arguments", false);
        newjob.setTime           ("start_date",              new DfTime(dt));
        newjob.setTime           ("expiration_date",         new DfTime(dt2));
        newjob.setInt            ("max_iterations",          0);
        newjob.setInt            ("run_interval",            5);
        newjob.setInt            ("run_mode",                1);
        newjob.setBoolean        ("is_inactive",             false);
        newjob.setString         ("target_server",           "mqadoc_test.mqadoc_test@mspsun51");
        newjob.setBoolean        ("run_now",                 false);
        newjob.setBoolean        ("inactivate_after_failure",false);
        newjob.setTime           ("a_next_invocation",       new DfTime(dt3));
        newjob.appendString      ("method_arguments",        "-docbase_name mqadoc_test");
        newjob.appendString      ("method_arguments",        "-dql \"SELECT r_object_id FROM mdt_scheduled_task WHERE m_exec_status = 'waiting' AND m_exec_date <= DATE(NOW)\"");
        newjob.save();
        
    }
    
}
