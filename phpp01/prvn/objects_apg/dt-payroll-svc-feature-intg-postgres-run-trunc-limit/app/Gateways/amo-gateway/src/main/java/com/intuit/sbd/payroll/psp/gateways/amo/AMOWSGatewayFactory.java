package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/15/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class AMOWSGatewayFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(AMOWSGatewayFactory.class);
    private static Class<? extends IAMOWSGateway> instanceClass = AMOWSGateway.class;

    public static void setInstanceClass(Class<? extends IAMOWSGateway> instanceClass) {
        AMOWSGatewayFactory.instanceClass = instanceClass;
    }

    public static IAMOWSGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("AMOWS Gateway could not be constructed.", t);
            return null;
        }
    }

}
