package com.intuit.sbd.payroll.psp.hibernate.criterion;

import org.hibernate.criterion.Criterion;
import org.hibernate.internal.util.collections.ArrayHelper;

/**
 * User: ihannur
 * Date: 12/18/13
 * Time: 2:28 AM
 */
public class CustomRestriction {

    public static Criterion sqlRestriction(String sql, String propertyName) {
        return new CustomSQLCriterion(sql, propertyName, ArrayHelper.EMPTY_OBJECT_ARRAY, ArrayHelper.EMPTY_TYPE_ARRAY);
    }
}
