package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;


/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/14/12
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class BRMGatewayFactory {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(BRMGatewayFactory.class);
    private static Class<? extends BRMGateway> instanceClass = BRMGateway.class;

    public static void setInstanceClass(Class<? extends BRMGateway> instanceClass) {
        BRMGatewayFactory.instanceClass = instanceClass;
    }

    public static BRMGateway createInstance() throws Exception {
        return instanceClass.newInstance();
    }
}
