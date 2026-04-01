package com.intuit.ems.payroll.psp.gateways.ers;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 17, 2010
 * Time: 2:57:46 PM
 */
public class ERSGatewayFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(ERSGatewayFactory.class);
    private static Class<? extends IERSGateway> instanceClass = ERSGateway.class;

    public static void setInstanceClass(Class<? extends IERSGateway> instanceClass) {
        ERSGatewayFactory.instanceClass = instanceClass;
    }

    public static IERSGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("ERS Gateway could not be constructed.", t);
            return null;
        }
    }
}
