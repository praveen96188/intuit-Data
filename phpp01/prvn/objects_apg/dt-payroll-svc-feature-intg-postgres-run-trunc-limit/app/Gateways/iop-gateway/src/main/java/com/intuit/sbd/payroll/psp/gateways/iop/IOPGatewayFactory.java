package com.intuit.sbd.payroll.psp.gateways.iop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeff Jones
 */
public class IOPGatewayFactory {

    private static final Logger logger = LoggerFactory.getLogger(IOPGatewayFactory.class);
    private static Class<? extends IIOPGateway> instanceClass = IOPGateway.class;

    public static void setInstanceClass(Class<? extends IOPGateway> instanceClass) {
        IOPGatewayFactory.instanceClass = instanceClass;
    }

    public static IIOPGateway createInstance(){
        try {
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.warn("IOP Gateway could not be constructed.", t);
            return null;
        }
    }

}
