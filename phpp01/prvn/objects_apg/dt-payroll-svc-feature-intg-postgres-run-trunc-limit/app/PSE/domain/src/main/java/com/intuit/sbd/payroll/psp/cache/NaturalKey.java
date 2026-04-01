package com.intuit.sbd.payroll.psp.cache;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jun 2, 2008
 * Time: 8:15:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class NaturalKey {
    private Class classDef;
    private Object[] keyValues;

    public NaturalKey(Class c, Object... keys) {
        classDef = c;
        keyValues = keys;
    }

    public Object[] getKeyValues() {
        // technically, should return a copy but trust no-one will mess with...
        return keyValues;
    }

    @Override
    public int hashCode() {
        int hash = classDef.getName().hashCode();
        for (Object keyValue : keyValues) {
            if (keyValue != null) {
                hash = hash & keyValue.hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NaturalKey)) return false;

        NaturalKey naturalKey = (NaturalKey) o;

        if (naturalKey == null ||
                !naturalKey.classDef.equals(this.classDef) ||
                naturalKey.keyValues.length != this.keyValues.length) {
            return false;
        }

        for (int i = 0; i < naturalKey.keyValues.length; i++) {
            if (!naturalKey.keyValues[i].equals(this.keyValues[i])) {
                return false;
            }
        }

        return true;
    }
}
