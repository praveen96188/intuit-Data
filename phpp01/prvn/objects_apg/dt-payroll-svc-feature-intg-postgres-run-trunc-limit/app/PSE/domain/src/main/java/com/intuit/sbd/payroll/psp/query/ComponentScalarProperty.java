package com.intuit.sbd.payroll.psp.query;

import java.util.HashMap;

/**
 * User: dweinberg
 * Date: Dec 4, 2009
 * Time: 3:10:14 PM
 */
public class ComponentScalarProperty<Q, V> extends ScalarProperty<Q, V>  {

    public ComponentScalarProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName) {
        super(parentDataEntityExpression, propertyName);
    }

    @Override
    public String getPropertyQueryPath() {
        return getParentDomainObjectExpression().getPropertyName() + "." + getPropertyName();
    }

    @Override
    String getAlias(HashMap<String, String> pPropertyPathToAliasMap, String rootAlias) {
        if (getPropertyPathList().size() == 2) {
            return rootAlias;
        } else {
            return super.getAlias(pPropertyPathToAliasMap, rootAlias);
        }
    }
}
