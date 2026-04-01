package com.intuit.sbd.payroll.psp.hibernate;

import org.hibernate.property.access.internal.PropertyAccessStrategyBasicImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 13, 2011
 * Time: 7:03:15 AM
 */
public class PSPMixedAccessor  implements PropertyAccessStrategy {

    private final PropertyAccessStrategyBasicImpl strategy;


    public PSPMixedAccessor(){
        PropertyAccessStrategyBasicImpl accessor = PropertyAccessStrategyBasicImpl.INSTANCE;
        this.strategy = accessor;

    }

    @Override
    public PropertyAccess buildPropertyAccess(Class containerJavaType, final String propertyName) {
        return new CustomPropertyAccessImpl( strategy, containerJavaType, propertyName );
    }

}

