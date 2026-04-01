package com.intuit.sbd.payroll.psp.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.Type;
import org.hibernate.internal.util.StringHelper;

/**
 * User: ihannur
 * Date: 12/18/13
 * Time: 2:29 AM
 */
public class CustomSQLCriterion implements Criterion {

    private final String sql;
    String propertyName;
    private final TypedValue[] typedValues;

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String[] columns = criteriaQuery.findColumns(propertyName, criteria);

        if (columns.length != 1) {
            throw new HibernateException("Custom SQL criteria may only be used with single-column property");
        }

        return StringHelper.replace(sql, "{" + propertyName + "}", columns[0]);
    }

    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return typedValues;
    }

    protected CustomSQLCriterion(String sql, String propertyName, Object[] values, Type[] types) {
        this.sql = sql;
        this.propertyName = propertyName;

        typedValues = new TypedValue[values.length];
        for (int i = 0; i < typedValues.length; i++) {
            typedValues[i] = new TypedValue(types[i], values[i]);
        }
    }
}
