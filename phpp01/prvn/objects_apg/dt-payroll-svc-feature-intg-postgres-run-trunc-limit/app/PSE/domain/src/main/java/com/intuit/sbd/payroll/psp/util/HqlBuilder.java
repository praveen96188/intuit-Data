package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import org.hibernate.Session;
import org.hibernate.type.Type;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: dweinberg
 * Date: 11/6/11
 * Time: 6:30 PM
 */
public class HqlBuilder {
    private List<ParameterSetter> parameters;
    private StringBuilder hql;

    private Boolean readOnly = null;

    public HqlBuilder() {
        parameters = new LinkedList<ParameterSetter>();
        hql = new StringBuilder();
    }

    public HqlBuilder(String string) {
        this();
        append(string);
    }

    public HqlBuilder(Boolean pReadOnly) {
        this();
        readOnly = pReadOnly;
    }

    public HqlBuilder(Boolean pReadOnly, String pHql) {
        this(pHql);
        readOnly = pReadOnly;
    }

    public HqlBuilder append(String string) {
        hql.append(string);
        if (! string.endsWith(" ")){
            hql.append(" ");
        }
        return this;
    }

    public HqlBuilder setParameter(String name, Object val, Type type) {
        parameters.add(new ParameterSetter(name, val, type));
        return this;
    }

    public HqlBuilder setParameter(String name, Object val) {
        return setParameter(name, val, null);
    }

    public HqlBuilder setParameterList(String name, Object... val) {
        return setParameter(name, val);
    }

    public HqlBuilder setParameterLike(String name, String val) {
        return setParameter(name,  "%" + val.toLowerCase() + "%", null);
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    private <T> List<T> list(Integer firstResult, Integer maxResults, String hqlString) {
        Session session = Application.getHibernateSession();
        //Execute Query
        org.hibernate.Query hibernateQuery = session.createQuery(hqlString);
        if (readOnly != null) {
            hibernateQuery.setReadOnly(readOnly);
        }

        if ((firstResult != null || maxResults != null) && Application.queryContainsCollectionFetches(hibernateQuery)) {
            //todo once all existing ones are eliminated, this should be a runtime exception
            Application.getLogger(HqlBuilder.class).warn("firstResult/maxResults specified with collection fetch; applying in memory! ALL rows will be returned from database.  Query: " + hibernateQuery.getQueryString());
        }

        if (firstResult != null) {
            hibernateQuery.setFirstResult(firstResult);
        }
        if (maxResults != null) {
            hibernateQuery.setMaxResults(maxResults);
        }

        for (ParameterSetter parameter : parameters) {
            if (parameter.type != null) {
                hibernateQuery.setParameter(parameter.parameter, parameter.value, parameter.type);
            } else if (parameter.value instanceof Collection) {
                hibernateQuery.setParameterList(parameter.parameter, (Collection) parameter.value);
            } else if (parameter.value instanceof Object[]) {
                hibernateQuery.setParameterList(parameter.parameter, (Object[]) parameter.value);
            } else {
                hibernateQuery.setParameter(parameter.parameter, parameter.value);
            }
        }

        List list = hibernateQuery.list();

        Application.checkResultSetTooLarge(hqlString, list.size());
        Application.getSessionCache().trackSqlCall(hqlString);

        //noinspection unchecked
        return list;
    }

    public <T> List<T> list(Integer firstResult, Integer maxResults) {
        return list(firstResult, maxResults, hql.toString());
    }

    public <T extends DomainEntity> DomainEntitySet<T> find(Integer firstResult, Integer maxResults) {
        List<T> list = list(firstResult, maxResults);
        return Application.getUniqueActualObjects(list);
    }

    public <T> List<T> list() {
        return list(null, null);
    }

    public <T extends DomainEntity> DomainEntitySet<T> find() {
        List<T> list = list();
        return Application.getUniqueActualObjects(list);
    }

    //generally for aggregates
    public <T> List<T> select(String select) {
        return list(null, null, select + " " + hql.toString());
    }

    //for distinct, etc.
    public <T> List<T> select(Integer firstResult, Integer maxResults, String select) {
        return list(firstResult, maxResults, select + " " + hql.toString());
    }


    private class ParameterSetter {
        public String parameter;
        public Object value;
        public Type type;

        private ParameterSetter(String parameter, Object value, Type type) {
            this.parameter = parameter;
            this.value = value;
            this.type = type;
        }
    }

}

