package com.intuit.ems.payroll.psp.gateways.ebs;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGateway;
import com.intuit.ems.payroll.psp.gateways.ers.IERSGateway;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 17, 2010
 * Time: 2:57:46 PM
 */
public class EBSGatewayFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(EBSGatewayFactory.class);
    private static Class<? extends IEBSGateway> instanceClass = EBSGateway.class;

    public static void setInstanceClass(Class<? extends IEBSGateway> instanceClass) {
        EBSGatewayFactory.instanceClass = instanceClass;
    }

    public static IEBSGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("EBS Gateway could not be constructed.", t);
            return null;
        }
    }
}
