package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 5:39:37 PM
 */
public class AMOGatewayFactory {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(AMOGatewayFactory.class);
    private static Class<? extends AbstractAMOGateway> instanceClass = AMOGateway.class;

    public static void setInstanceClass(Class<? extends AbstractAMOGateway> instanceClass) {
        AMOGatewayFactory.instanceClass = instanceClass;
    }

    public static AbstractAMOGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("AMO Gateway could not be constructed.", t);
            return null;
        }
    }
}
