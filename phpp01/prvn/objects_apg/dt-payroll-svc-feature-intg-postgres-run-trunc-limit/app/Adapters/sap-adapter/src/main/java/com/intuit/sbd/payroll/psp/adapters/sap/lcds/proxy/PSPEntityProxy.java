/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/lcds/proxy/PSPEntityProxy.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.lcds.proxy;

import com.intuit.sbd.payroll.psp.adapters.sap.lcds.DataServiceExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.io.BeanProxy;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;

/**
 * PSP Entity Proxy acts as a proxy for translating java objects to and from ActionScript objects.  The
 * purpose of this class is to override the default marshalization functionality for types that cannot
 * be handled by ActionScript.
 *
 * @author Joe Warmelink
 */
public class PSPEntityProxy extends BeanProxy {
    private static SpcfLogger logger = PayrollServices.getLogger(PSPEntityProxy.class);
    private static DataServiceExceptionFactory dseFactory = new DataServiceExceptionFactory(logger);

    public PSPEntityProxy() {
        super();
        this.setIncludeReadOnly(true);
    }

    public void setValue(Object instance, String propertyName, Object value) {
        try {
            BeanUtilsBean bub = new BeanUtilsBean();
            bub.getPropertyUtils().addBeanIntrospector(
                    SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS);
            Class propClass = bub.getPropertyUtils().getPropertyType(instance, propertyName);
            if (propClass == null) {
                if (!"name".equals(propertyName)) {
                    dseFactory.throwPropertyDoesNotExistException("setValue()", propertyName, instance.getClass());
                } else {
                    return;
                }
            }

            if (propClass.isEnum() && value instanceof String) // if this is an enum
            {
                Enum enumValue = Enum.valueOf(propClass, (String) value);
                super.setValue(instance, propertyName, enumValue);
            } else {
                super.setValue(instance, propertyName, value);
            }
        } catch (Exception ex) {
            dseFactory.throwEntityProxyException("setValue()", ex);
        }
    }

    public Object getValue(Object instance, String propertyName) {
        Object returnValue = super.getValue(instance, propertyName);

        if (returnValue instanceof Enum) {
            returnValue = returnValue.toString();
        }

        return returnValue;
    }

    public Object clone() {
        PSPEntityProxy clone = new PSPEntityProxy();
        clone.setCloneFieldsFrom(this);
        return clone;
    }

}
