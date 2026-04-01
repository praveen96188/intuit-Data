package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import org.apache.commons.lang.StringUtils;

/**
 * User: rkrishna
 * Date: Apr 7, 2008
 * Time: 4:53:16 PM
 */
public class SalesTaxGatewayFactory {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(SalesTaxGatewayFactory.class);
    private static Class<? extends ISalesTaxGateway> instanceClass = SalesTaxGatewayImpl.class;

    public static void setInstanceClass(Class<? extends ISalesTaxGateway> instanceClass) {
        SalesTaxGatewayFactory.instanceClass = instanceClass;
    }

    public static ISalesTaxGateway createISalesTaxGateway(){
        return createISalesTaxGateway(null);
    }

    public static ISalesTaxGateway createISalesTaxGateway(String overrideClass){
        try {
            if (StringUtils.isNotEmpty(overrideClass)) {
                Class<?> overrideClazz = Class.forName(overrideClass);
                return (ISalesTaxGateway) overrideClazz.newInstance();
            }
            return instanceClass.newInstance();
        } catch (Throwable t) {
            logger.info("Sales Tax Gateway could not be constructed.", t);
            return null;
        }
    }
}
