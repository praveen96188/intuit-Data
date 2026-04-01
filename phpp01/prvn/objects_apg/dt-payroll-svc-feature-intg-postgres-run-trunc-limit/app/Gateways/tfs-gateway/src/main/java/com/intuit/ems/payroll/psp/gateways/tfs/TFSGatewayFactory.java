package com.intuit.ems.payroll.psp.gateways.tfs;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/27/12
 * Time: 11:23 AM
 */
public class TFSGatewayFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(TFSGatewayFactory.class);
    private static Class<? extends ITFSGateway> instanceClass = TFSGateway.class;

    public static void setInstanceClass(Class<? extends ITFSGateway> instanceClass) {
        TFSGatewayFactory.instanceClass = instanceClass;
    }

    public static ITFSGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.error("TFS Gateway could not be constructed.", t);
            return null;
        }
    }

}
